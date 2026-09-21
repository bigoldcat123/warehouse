package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.PO.HouseInfor;
import com.example.demo.system.mapper.HouseInforMapper;
import com.example.demo.system.service.IHouseInforService;
import org.springframework.stereotype.Service;

/**
 * 仓房粮情信息服务实现。
 */
@Service
public class HouseInforServiceImpl extends ServiceImpl<HouseInforMapper, HouseInfor>
        implements IHouseInforService {

    @Override
    public HouseInfor getLatestByHouseNo(String houseNo) {
        QueryWrapper<HouseInfor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("HouseNo", houseNo)
                .orderByDesc("TestDate")
                .last("LIMIT 1");
        return getOne(queryWrapper, false);
    }
}
