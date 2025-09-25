package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.mapper.EntryMapper;
import com.example.demo.system.mapper.HouseMapper;
import com.example.demo.system.service.IHouseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements IHouseService {

    @Autowired
    HouseMapper houseMapper;

    @Override
    public House getHouseByNo(String houseNo) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("HouseNo",houseNo);

        return this.getOne(queryWrapper);

    }

}
