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
     */
    @GetMapping
    public R list(Integer current, Integer size, String houseNo) {
        QueryWrapper<WarehouseSettings> queryWrapper = new QueryWrapper<>();
        if (houseNo != null && !houseNo.isEmpty()) {
            queryWrapper.eq("house_no", houseNo);
        }
        Page<WarehouseSettings> page = warehouseSettingsService.page(new Page<>(current, size), queryWrapper);
        return R.ok(page);
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