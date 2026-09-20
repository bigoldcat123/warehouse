package com.example.demo.system.service;

import com.example.demo.system.entity.PO.ReceiverData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
public interface IReceiverDataService extends IService<ReceiverData> {

    /**
     * 查询温度二维数据，数组结构为 [串][层]。
     */
    List<List<Double>> getTemperatureMatrix(String houseNo, LocalDateTime testDate);

    /**
     * 查询湿度二维数据，数组结构为 [串][层]。
     */
    List<List<Double>> getHumidityMatrix(String houseNo, LocalDateTime testDate);

    /**
     * 查询 PH3 二维数据，数组结构为 [串][层]。
     */
    List<List<Double>> getPh3Matrix(String houseNo, LocalDateTime testDate);
}
