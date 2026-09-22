package com.example.demo.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.system.entity.PO.Xunzheng;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

/**
 * 熏蒸记录服务。
 */
public interface IXunzhengService extends IService<Xunzheng> {

    /**
     * 判断仓房在指定日期内是否处于熏蒸状态。
     *
     * @param houseNo 仓房编号
     * @param date 查询日期
     * @return 当天任意时刻存在熏蒸记录时返回 true
     */
    boolean isFumigating(String houseNo, LocalDate date);

    /**
     * 批量查询多个仓房在指定日期的熏蒸状态。
     */
    Map<String, Boolean> getFumigationStatuses(List<String> houseNos, LocalDate date);
}
