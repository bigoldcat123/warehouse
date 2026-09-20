package com.example.demo.system.service;

import com.example.demo.system.entity.PO.ReceiverData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.DTO.DataDTO;

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
     * 将采集记录转换为数据列表 DTO，并计算各层 PH3 统计值。
     */
    List<DataDTO> parseDataDTO(List<ReceiverData> records);

    /**
     * 查询仓房所有 TestDate，去重后按时间升序排列。
     */
    List<String> getTestDates(String houseNo);

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
