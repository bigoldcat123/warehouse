package com.example.demo.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.PO.HouseInfor;

/**
 * 仓房粮情信息服务。
 */
public interface IHouseInforService extends IService<HouseInfor> {

    /**
     * 获取仓房最新一条粮情信息。
     */
    HouseInfor getLatestByHouseNo(String houseNo);
}
