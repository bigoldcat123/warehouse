package com.example.demo.system.service.impl;

import com.example.demo.system.entity.PO.Entry;
import com.example.demo.system.mapper.EntryMapper;
import com.example.demo.system.service.IEntryService;
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
public class EntryServiceImpl extends ServiceImpl<EntryMapper, Entry> implements IEntryService {

    @Autowired
    EntryMapper entryMapper;

    @Override
    public Entry getNewest(Integer houseId) {
        return entryMapper.getNewest(houseId);
    }
}
