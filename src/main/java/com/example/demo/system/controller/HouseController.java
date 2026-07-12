package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.DTO.HouseDTO;
import com.example.demo.system.entity.DTO.TongFengDTO;
import com.example.demo.system.entity.PO.*;
import com.example.demo.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 *
 * 添加、删除、修改仓房信息
 */
@RestController
@RequestMapping("/house")
public class HouseController {

    @Autowired
    IHouseService houseService;

    @Autowired
    IWarehouseService warehouseService;
    @Autowired
    IEntryService entryService;
    @Autowired
    ITongfengDevService tongfengDevService;

    @Autowired
    IDataService dataService;
    @Autowired
    IAlarmService alarmService;
    @Autowired
    IGfKtService gfKtService;

    @GetMapping
    public R findAll(Integer current, Integer size,Integer wareHouseId,String houseNo) {

        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        if(wareHouseId !=null)
            queryWrapper.in("warehouseID", wareHouseId);
        if(houseNo != null && !houseNo.isEmpty())
            queryWrapper.like("HouseNo", houseNo);

        Page<House> page = houseService.page(new Page<>(current, size),queryWrapper);
        List<HouseDTO> list = page.getRecords().stream().map(x -> x.toDTO(entryService,gfKtService)).toList();

        Page<HouseDTO> p = new Page<>();
        p.setRecords(list);
        p.setCurrent(current);
        p.setSize(size);
        p.setTotal(page.getTotal());

        return R.ok(p);
    }

    @GetMapping("yuntu")
    public R listAllYuntu(Integer wareHouseId) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("warehouseID", wareHouseId);

        List<House> houses = houseService.list(queryWrapper);
        List<String> list = houses.stream().filter(x -> x.getYuntu() != null && !x.getYuntu().isBlank()).map(h -> {
            String[] urls = h.getYuntu().split(",");
            return urls[0];
        }).toList();

        return R.ok(list);
    }
    @GetMapping("/wind")
    public R findAllWind(Integer current, Integer size,Integer wareHouseId,String houseNo) {

        Warehouse warehouse = warehouseService.getById(wareHouseId);
        QueryWrapper<TongfengDev> queryWrapper = new QueryWrapper<>();
//        if (!CurrentUser.isMainCompany()) {
        if(warehouse !=null)
            queryWrapper.eq("WareHouseNo",warehouse.getWarehouseNo());
        else {
            R.errorShow("每天这个warehouse！");
        }
        if(houseNo != null && !houseNo.isEmpty())
            queryWrapper.like("HouseNo", houseNo);
//        }
        Page<TongfengDev> page = tongfengDevService.page(new Page<>(current, size),queryWrapper);
        List<HouseDTO> list = page.getRecords().stream().map(x -> x.toHouse(houseService)).filter(Objects::nonNull).map(x -> x.toDTO(entryService,null)).toList();

        Page<HouseDTO> p = new Page<>();
        p.setRecords(list);
        p.setCurrent(current);
        p.setSize(size);
        p.setTotal(page.getTotal());

        return R.ok(p);
    }

    @PostMapping()
    public R add (@RequestBody House house) {
        Warehouse warehouse = warehouseService.getById(house.getWarehouseID());
        if(warehouse==null){
            return R.error();
        }

        boolean save = houseService.save(house);
        return save ? R.ok() : R.error();
    }

    @PutMapping
    public R update (@RequestBody House house) {
        boolean b = houseService.updateById(house);
        if (!b) {
            return R.errorShow("没有此条记录");
        }
        return R.ok();
    }
    @DeleteMapping("{id}")
    public R delete (@PathVariable Long id) {





        boolean b = houseService.removeById(id);
        if (b){

//
//            alarmService.remove(new QueryWrapper<Alarm>().eq("HouseNo",id));
//            dataService.remove(new QueryWrapper<Data>().eq("HouseNo", id));
            return R.ok();
        }else {
            return R.error();
        }
    }

    @GetMapping("kv")
    public R kv() {
        Object[] array = houseService.list().stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getHouseName());
            return map;
        }).toArray();
        return R.ok(array);
    }

    @GetMapping("/kv/{warehouseId}")
    public R findByWarehouseId(@PathVariable Long warehouseId) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("warehouseID", warehouseId);
        Object[] array = houseService.list(queryWrapper).stream().map(x -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", x.getId());
            map.put("value", x.getHouseName());
            return map;
        }).toArray();
        return R.ok(array);
    }

    @GetMapping("/tongfeng")
    public R tongfengInfo(Integer houseId) {
        House house = houseService.getById(houseId);
        if (house == null){
            return R.errorShow("没有这个仓房");
        }
        return R.ok(TongFengDTO.fromHouse(house));
    }

}
