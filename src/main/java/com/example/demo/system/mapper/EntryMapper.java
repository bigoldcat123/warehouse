package com.example.demo.system.mapper;

import com.example.demo.system.entity.PO.Entry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
public interface EntryMapper extends BaseMapper<Entry> {

    Entry getNewest(@Param("houseID") Integer houseId);
}
