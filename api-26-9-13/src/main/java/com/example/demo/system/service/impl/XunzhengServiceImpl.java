package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.PO.Xunzheng;
import com.example.demo.system.mapper.XunzhengMapper;
import com.example.demo.system.service.IXunzhengService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 熏蒸记录服务实现。
 */
@Service
public class XunzhengServiceImpl extends ServiceImpl<XunzhengMapper, Xunzheng>
        implements IXunzhengService {

    @Override
    public boolean isFumigating(String houseNo, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();

        QueryWrapper<Xunzheng> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("HouseNo", houseNo)
                .isNotNull("start_DT")
                .lt("start_DT", nextDayStart)
                .and(wrapper -> wrapper.isNull("stop_DT").or().ge("stop_DT", dayStart));
        return count(queryWrapper) > 0;
    }
}
