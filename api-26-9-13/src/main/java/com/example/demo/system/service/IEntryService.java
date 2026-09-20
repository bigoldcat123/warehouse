package com.example.demo.system.service;

import com.example.demo.system.entity.PO.Entry;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
public interface IEntryService extends IService<Entry> {

    Entry getNewest(Integer houseNO);
}
