package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.PO.Xunzheng;
import com.example.demo.system.mapper.XunzhengMapper;
import com.example.demo.system.service.IXunzhengService;
import org.springframework.stereotype.Service;

/**
 * 熏蒸记录服务实现。
 */
@Service
public class XunzhengServiceImpl extends ServiceImpl<XunzhengMapper, Xunzheng>
        implements IXunzhengService {
}
