package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.WarehouseSettings;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IWarehouseSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2026-09-02
 *
 * 仓房基础信息设置：新增、删除、修改、查询
 */
@RestController
@RequestMapping("/warehouseSettings")
public class WarehouseSettingsController {

    @Autowired
    IWarehouseSettingsService warehouseSettingsService;
    @Autowired
    IHouseService houseService;

    /**
     * 分页查询仓房设置
     * @param current 当前页（从1开始）
     * @param size 每页条数
     * @param houseNo 仓房编号（可选，精确匹配）
     * @param warehouseID 仓库ID（可选，过滤该仓库下的仓房设置）
     */
    @GetMapping
    public R list(Integer current, Integer size, String houseNo, String warehouseID) {
        QueryWrapper<WarehouseSettings> queryWrapper = new QueryWrapper<>();
        if (houseNo != null && !houseNo.isEmpty()) {
            queryWrapper.eq("house_no", houseNo);
        }
        if (warehouseID != null) {
            List<String> houseNos = houseService.list(
                            new QueryWrapper<House>().eq("warehouseID", warehouseID))
                    .stream()
                    .map(House::getHouseNo)
                    .toList();
            if (houseNos.isEmpty()) {
                return R.ok(new Page<>(current, size));
            }
            queryWrapper.in("house_no", houseNos);
        }
        Page<WarehouseSettings> page = warehouseSettingsService.page(new Page<>(current, size), queryWrapper);
        return R.ok(page);
    }

    /**
     * 查询指定仓库下尚未设置基础信息的仓房列表
     * （house 与 warehouse_settings 为一对一关系，已设置的仓房会被过滤）
     * @param warehouseId 仓库ID
     * @return 未设置的仓房列表
     */
    @GetMapping("/unset/{warehouseId}")
    public R listUnset(@PathVariable String warehouseId) {
        QueryWrapper<House> houseQuery = new QueryWrapper<>();
        houseQuery.eq("warehouseID", warehouseId);
        List<House> houses = houseService.list(houseQuery);
        if (houses.isEmpty()) {
            return R.ok(Collections.emptyList());
        }
        List<String> setHouseNos = warehouseSettingsService
                .listObjs(new QueryWrapper<WarehouseSettings>().select("house_no"))
                .stream()
                .map(Object::toString)
                .toList();
        List<House> unset = houses.stream()
                .filter(h -> !setHouseNos.contains(h.getHouseNo()))
                .toList();
        return R.ok(unset);
    }

    /**
     * 根据主键查询仓房设置
     * @param id 主键ID
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable Integer id) {
        WarehouseSettings settings = warehouseSettingsService.getById(id);
        if (settings == null) {
            return R.errorShow("没有此条记录");
        }
        return R.ok(settings);
    }

    /**
     * 根据仓房编号查询仓房设置（仓房编号唯一）
     * @param houseNo 仓房编号
     */
    @GetMapping("/houseNo/{houseNo}")
    public R getByHouseNo(@PathVariable String houseNo) {
        WarehouseSettings settings = warehouseSettingsService.getByHouseNo(houseNo);
        if (settings == null) {
            return R.errorShow("没有此仓房的设置");
        }
        return R.ok(settings);
    }

    /**
     * 新增仓房设置
     * @param settings 仓房设置信息（houseNo 必填）
     */
    @PostMapping
    public R add(@RequestBody WarehouseSettings settings) {
        if (settings.getHouseNo() == null || settings.getHouseNo().isEmpty()) {
            return R.errorShow("仓房编号不能为空");
        }
        House house = houseService.getHouseByNo(settings.getHouseNo());
        if (house == null) {
            return R.errorShow("没有该仓房");
        }
        if (warehouseSettingsService.getByHouseNo(settings.getHouseNo()) != null) {
            return R.errorShow("该仓房设置已存在");
        }
        boolean save = warehouseSettingsService.save(settings);
        return save ? R.ok() : R.error();
    }

    /**
     * 修改仓房设置（按主键 id 更新，id 必填）
     * @param settings 仓房设置信息
     */
    @PutMapping
    public R update(@RequestBody WarehouseSettings settings) {
        boolean b = warehouseSettingsService.updateById(settings);
        if (!b) {
            return R.errorShow("没有此条记录");
        }
        return R.ok();
    }

    /**
     * 删除仓房设置
     * @param id 主键ID
     */
    @DeleteMapping("{id}")
    public R delete(@PathVariable Integer id) {
        boolean b = warehouseSettingsService.removeById(id);
        return b ? R.ok() : R.error();
    }
}
