package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.Alarm;
import com.example.demo.system.entity.PO.Data;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Warehouse;
import com.example.demo.system.service.IAlarmService;
import com.example.demo.system.service.IDataService;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IWarehouseService;
import com.mysql.cj.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 *
 * 添加、删除、修改仓库信息
 */
@RestController
    @RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    IWarehouseService warehouseService;
    @Autowired
    IHouseService houseService;


    @Autowired
    IDataService dataService;
    @Autowired
    IAlarmService alarmService;

    @GetMapping()
    public R findAll(Integer current, Integer size) {
        Page<Warehouse> page = warehouseService.page(new Page<Warehouse>(current, size), null);
        return R.ok(page);
    }
    @GetMapping("kv")
    public R kv() {
        Object[] array = warehouseService.list().stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getWarehouseName());
            return map;
        }).toArray();
        return R.ok(array);
    }
    @GetMapping("belong/kv")
    public R belongKv() {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        Integer companyID = CurrentUser.get().getCompanyID();
        if(!CurrentUser.isMainCompany()) {
            queryWrapper.eq("id", companyID);
        }
        Object[] array = warehouseService.list(queryWrapper).stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getWarehouseName());
            map.put("no", x.getWarehouseNo());
            return map;
        }).toArray();
        return R.ok(array);
    }


    @PostMapping()
    public R add (@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return R.ok();
    }


    @PutMapping
    public R update (@RequestBody Warehouse warehouse) {
        boolean b = warehouseService.updateById(warehouse);
        if (!b) {
            return R.error();
        }
        return R.ok();
    }
    @DeleteMapping("{id}")
    public R delete (@PathVariable Long id) {




        List<House> list = houseService.list(new QueryWrapper<House>().eq("warehouseID", id));


        boolean b = warehouseService.removeById(id);
        boolean warehouseID = houseService.remove(new QueryWrapper<House>().eq("warehouseID", id));
        if (b){


            list.forEach(x-> {
//                alarmService.remove(new QueryWrapper<Alarm>().eq("HouseNo", x.getId()));
//                dataService.remove(new QueryWrapper<Data>().eq("HouseNo", x.getId()));
                houseService.removeById(x.getId());
            });


            return R.okShow("删除成功");
        }else {
            return R.errorShow("删除失败");
        }
    }
    @GetMapping("/getWarehouseIdBy/{houseId}")
    public R getWarehouseIdBy(@PathVariable Long houseId) {
        House house = houseService.getById(houseId);
        return R.ok(house.getWarehouseID());
    }
}
