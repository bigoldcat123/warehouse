package com.example.demo.system.service;

import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.DTO.HouseTempRecordDTO;
import com.example.demo.system.entity.PO.Data;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author czh
 * @since 2024-07-28
 */
public interface IDataService extends IService<Data> {

    List<DataDTO> parseDTO(List<Data> list);

    DataDetailDTO getDataDetail(Integer id);

    /**
     * 根据粮房编号和指定点坐标，返回该点的温度记录数组
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     * @param hang 行坐标（从1开始）
     * @param lie 列坐标（从1开始）
     * @return [{testDate, temp}] 每条记录包含检测日期和该点的温度值
     */
    List<HouseTempRecordDTO> getTempRecordsByHouseNo(String houseNo, int ceng, int hang, int lie);

    /**
     * 根据粮房编号和层坐标，返回该层每个时间点的平均温度
     * @param houseNo 粮房编号
     * @param ceng 层坐标（从1开始）
     * @return [{testDate, temp}] 每条记录包含检测日期和该层的平均温度
     */
    List<HouseTempRecordDTO> getLayerAvgTempByHouseNo(String houseNo, int ceng);

    /**
     * 根据粮房编号，返回每个时间点全部温度点的平均值
     * @param houseNo 粮房编号
     * @return [{testDate, temp}] 每条记录包含检测日期和全部点的平均温度
     */
    List<HouseTempRecordDTO> getAllAvgTempByHouseNo(String houseNo);

    /**
     * 批量插入随机温度数据（用于测试）
     * @param houseNo 粮房编号
     * @param count 插入记录条数
     * @param x 行数(hang)
     * @param y 列数(lie)
     * @param z 层数(ceng)
     * @return 插入的记录数
     */
    int insertRandomData(String houseNo, int count, int x, int y, int z);
    /**
     * 根据粮房编号，返回每个时间点的三维温度数组
     * 源数据按 # 分割记录，按 $ 分割：列:行:层$温度$5
     * @param houseNo 粮房编号
     * @return {采集时间: 三维温度数组[层][行][列]}，按时间升序
     */
    Map<LocalDateTime, List<List<List<String>>>> getTemperatureCubeByHouseNo(String houseNo);
}
