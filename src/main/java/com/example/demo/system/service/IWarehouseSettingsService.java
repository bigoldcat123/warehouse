package com.example.demo.system.service;

import com.example.demo.system.entity.PO.WarehouseSettings;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author czh
 * @since 2026-09-02
 */
public interface IWarehouseSettingsService extends IService<WarehouseSettings> {

    /**
     * 根据仓房编号查询设置（仓房编号唯一）
     * @param houseNo 仓房编号
     * @return 设置记录，不存在返回 null
     */
    WarehouseSettings getByHouseNo(String houseNo);
}