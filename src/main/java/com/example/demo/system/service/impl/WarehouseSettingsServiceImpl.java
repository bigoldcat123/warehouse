package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.PO.WarehouseSettings;
import com.example.demo.system.mapper.WarehouseSettingsMapper;
import com.example.demo.system.service.IWarehouseSettingsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author czh
 * @since 2026-09-02
 */
@Service
public class WarehouseSettingsServiceImpl extends ServiceImpl<WarehouseSettingsMapper, WarehouseSettings> implements IWarehouseSettingsService {

    @Override
    public WarehouseSettings getByHouseNo(String houseNo) {
        QueryWrapper<WarehouseSettings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("house_no", houseNo);
        return this.getOne(queryWrapper);
    }
}