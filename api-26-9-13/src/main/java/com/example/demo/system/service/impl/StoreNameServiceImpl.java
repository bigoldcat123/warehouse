package com.example.demo.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.system.entity.PO.StoreName;
import com.example.demo.system.mapper.StoreNameMapper;
import com.example.demo.system.service.IStoreNameService;
import org.springframework.stereotype.Service;

/**
 * 粮库名称服务实现。
 */
@Service
public class StoreNameServiceImpl extends ServiceImpl<StoreNameMapper, StoreName>
        implements IStoreNameService {
}
