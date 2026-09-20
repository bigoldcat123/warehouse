package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.PO.PointDefine;
import com.example.demo.system.mapper.PointDefineMapper;
import com.example.demo.system.service.IPointDefineService;
import org.springframework.stereotype.Service;

/**
 * 仓房测点定义服务实现。
 */
@Service
public class PointDefineServiceImpl extends ServiceImpl<PointDefineMapper, PointDefine>
        implements IPointDefineService {
}
