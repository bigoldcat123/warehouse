package com.example.demo.system.service;

import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
import com.example.demo.system.entity.DTO.HouseTempRecordDTO;
import com.example.demo.system.entity.PO.Data;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
     * 根据粮房编号和维度(x,y,z)返回温度记录数组
     * @param houseNo 粮房编号
     * @param x 行数
     * @param y 列数
     * @param z 层数
     * @return [{testDate, temp}] 每条记录包含检测日期和温度数组
     */
    List<HouseTempRecordDTO> getTempRecordsByHouseNo(String houseNo, int x, int y, int z);
}
