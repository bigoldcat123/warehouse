package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.StoreName;
import com.example.demo.system.service.IHouseService;
import com.example.demo.system.service.IStoreNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    IStoreNameService storeNameService;
    @Autowired
    IHouseService houseService;


    @GetMapping()
    public R findAll(Integer current, Integer size) {
        Page<StoreName> page = storeNameService.page(new Page<>(current, size), null);
        return R.ok(page);
    }
    @GetMapping("kv")
    public R kv() {
        Object[] array = storeNameService.list().stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getStoreNo());
            map.put("value", x.getStoreName());
            return map;
        }).toArray();
        return R.ok(array);
    }
    @GetMapping("belong/kv")
    public R belongKv() {
        QueryWrapper<StoreName> queryWrapper = new QueryWrapper<>();
        String companyID = CurrentUser.get().getCompanyID();
        if(!CurrentUser.isMainCompany()) {
            queryWrapper.eq("StoreNo", companyID);
        }
        Object[] array = storeNameService.list(queryWrapper).stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getStoreNo());
            map.put("value", x.getStoreName());
            map.put("no", x.getStoreNo());
            return map;
        }).toArray();
        return R.ok(array);
    }


    @PostMapping()
    public R add (@RequestBody StoreName storeName) {
        storeNameService.save(storeName);
        return R.ok();
    }


    @PutMapping
    public R update (@RequestBody StoreName storeName) {
        boolean b = storeNameService.updateById(storeName);
        if (!b) {
            return R.error();
        }
        return R.ok();
    }
    @DeleteMapping("{id}")
    public R delete (@PathVariable String id) {




        List<House> list = houseService.list(new QueryWrapper<House>().eq("warehouseID", id));


        boolean b = storeNameService.removeById(id);
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
