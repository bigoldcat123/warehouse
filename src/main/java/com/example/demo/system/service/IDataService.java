package com.example.demo.system.service;

import com.example.demo.system.entity.DTO.DataDTO;
import com.example.demo.system.entity.DTO.DataDetailDTO;
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
}
