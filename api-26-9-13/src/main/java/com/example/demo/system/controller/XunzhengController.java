package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Xunzheng;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IXunzhengService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 熏蒸记录接口。
 */
@RestController
@RequestMapping("/xunzheng")
public class XunzhengController {

    @Autowired
    IXunzhengService xunzhengService;
    @Autowired
    IHouseService houseService;

    /**
     * 分页查询熏蒸记录，可按当前仓库和仓房筛选。
     */
    @GetMapping
    public R list(Integer current, Integer size, String warehouseID, String houseNo) {
        String effectiveWarehouseID = CurrentUser.isMainCompany()
                ? warehouseID
                : CurrentUser.get().getCompanyID();
        QueryWrapper<Xunzheng> queryWrapper = new QueryWrapper<>();
        if (effectiveWarehouseID != null && !effectiveWarehouseID.isBlank()) {
            List<String> houseNos = houseService.list(
                            new QueryWrapper<House>().eq("warehouseID", effectiveWarehouseID))
                    .stream()
                    .map(House::getHouseNo)
                    .toList();
            if (houseNos.isEmpty()) {
                return R.ok(new Page<Xunzheng>(current, size));
            }
            queryWrapper.in("HouseNo", houseNos);
        }
        if (houseNo != null && !houseNo.isBlank()) {
            queryWrapper.eq("HouseNo", houseNo);
        }
        queryWrapper.orderByDesc("start_DT").orderByDesc("id");
        return R.ok(xunzhengService.page(new Page<>(current, size), queryWrapper));
    }

    @PostMapping
    public R add(@RequestBody Xunzheng xunzheng) {
        R validationResult = validate(xunzheng);
        if (validationResult != null) {
            return validationResult;
        }
        xunzheng.setId(null);
        xunzheng.setSetDatetime(LocalDateTime.now());
        return xunzhengService.save(xunzheng) ? R.ok() : R.errorShow("新增失败");
    }

    @PutMapping
    public R update(@RequestBody Xunzheng xunzheng) {
        if (xunzheng.getId() == null) {
            return R.errorShow("熏蒸记录ID不能为空");
        }
        R validationResult = validate(xunzheng);
        if (validationResult != null) {
            return validationResult;
        }
        xunzheng.setSetDatetime(LocalDateTime.now());
        return xunzhengService.updateById(xunzheng) ? R.ok() : R.errorShow("没有此条记录");
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable Integer id) {
        return xunzhengService.removeById(id) ? R.ok() : R.errorShow("没有此条记录");
    }

    /**
     * 判断仓房在指定日期是否正在熏蒸，未传日期时查询今天。
     *
     * @param houseNo 仓房编号
     * @param date 日期，格式 yyyy-MM-dd
     */
    @GetMapping("/isFumigating")
    public R isFumigating(
            @RequestParam("houseNo") String houseNo,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (houseNo == null || houseNo.isBlank()) {
            return R.errorShow("仓房编号不能为空");
        }
        LocalDate queryDate = date == null ? LocalDate.now() : date;
        return R.ok(xunzhengService.isFumigating(houseNo, queryDate));
    }

    private R validate(Xunzheng xunzheng) {
        if (xunzheng.getHouseNo() == null || xunzheng.getHouseNo().isBlank()) {
            return R.errorShow("仓房编号不能为空");
        }
        House house = houseService.getHouseByNo(xunzheng.getHouseNo());
        if (house == null) {
            return R.errorShow("仓房不存在");
        }
        if (!CurrentUser.isMainCompany()
                && !CurrentUser.get().getCompanyID().equals(house.getWarehouseID())) {
            return R.errorShow("不能操作其他仓库的仓房");
        }
        if (xunzheng.getStartDt() == null) {
            return R.errorShow("开始时间不能为空");
        }
        if (xunzheng.getStopDt() != null && xunzheng.getStopDt().isBefore(xunzheng.getStartDt())) {
            return R.errorShow("结束时间不能早于开始时间");
        }
        return null;
    }
}
