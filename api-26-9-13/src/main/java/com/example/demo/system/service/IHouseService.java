package com.example.demo.system.service;

import com.example.demo.system.entity.PO.House;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
public interface IHouseService extends IService<House> {
    House getHouseByNo(String houseNo);

}
