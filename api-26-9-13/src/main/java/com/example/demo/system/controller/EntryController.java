package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.CurrentUser;
import com.example.demo.common.R;
import com.example.demo.system.entity.PO.Alarm;
import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.entity.PO.Warehouse;
import com.example.demo.system.entity.query.EntryQuery;
import com.example.demo.system.service.IEntryService;
import com.example.demo.system.service.IHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 * 入库操作，已入库列表，删除，修改，选择某一个仓房入库，入库信
 */
@RestController
@RequestMapping("/entry")
public class EntryController {


    @Autowired
    IEntryService entryService;
    @Autowired
    IHouseService houseService;

    @PostMapping()
    public R add (@RequestBody Entry entry) {
        House house = houseService.getById(entry.getHouseID());
        if(house == null){
            return R.errorShow("没有该仓房");
        }
        CurrentUser currentUser = CurrentUser.get();
        entry.setEntryUserId(currentUser.getId());
        boolean save = entryService.save(entry);
        return save ? R.ok() : R.error();
    }

    @PutMapping
    public R update (@RequestBody Entry entry) {
        boolean b = entryService.updateById(entry);
        if (!b) {
            return R.errorShow("没有此条记录");
        }
        return R.ok();
    }
    @DeleteMapping("{id}")
    public R delete (@PathVariable Long id) {
        boolean b = entryService.removeById(id);
        if (b){
            return R.ok();
        }else {
            return R.error();
        }
    }
    //@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size
    @PostMapping("/{current}/{size}")
    public R list(@RequestBody EntryQuery entryQuery, @PathVariable Integer current, @PathVariable Integer size) {
        Integer companyID = CurrentUser.get().getCompanyID();
        QueryWrapper<House> houseQueryWrapper = new QueryWrapper<>();
        if(!CurrentUser.isMainCompany()){
            houseQueryWrapper.eq("warehouseID", companyID);
        }

        QueryWrapper<Entry> entryQueryWrapper = new QueryWrapper<>();

        if(entryQuery != null && entryQuery.getWarehouseId()!=null && entryQuery.getHouseId() == null){
            QueryWrapper<House> houseQueryWrapper1 = new QueryWrapper<>();
            houseQueryWrapper1.eq("warehouseID", entryQuery.getWarehouseId());

            List<Integer> list = houseService.list(houseQueryWrapper1).stream().map(House::getId).toList();
            if(list.isEmpty()){
                Page<Entry> page = new Page<>();
                page.setTotal(0);
                page.setPages(0);
                return  R.ok(page);
            }
            entryQueryWrapper.in("houseID",list);
        }
        if(entryQuery != null && entryQuery.getHouseId()!=null){
            entryQueryWrapper.eq("houseID",entryQuery.getHouseId());
        }
        if(entryQuery != null && entryQuery.getTo()!=null){
            entryQueryWrapper.le("entry_time",entryQuery.getTo());
        }
        if(entryQuery != null && entryQuery.getFrom()!=null){
            entryQueryWrapper.ge("entry_time",entryQuery.getFrom());
        }

        List<House> list = houseService.list(houseQueryWrapper);
        entryQueryWrapper.in("houseID",list.stream().map(House::getId).toArray());

        Page<Entry> page = entryService.page(new Page<>(current, size),entryQueryWrapper);
        return R.ok(page);
    }
}
