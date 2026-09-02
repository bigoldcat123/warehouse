package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>
 *  仓房基础信息设置表
 * </p>
 *
 * @author czh
 * @since 2026-09-02
 */
@Getter
@Setter
@ToString
@TableName("warehouse_settings")
public class WarehouseSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 仓房编号
     */
    @TableField("house_no")
    private String houseNo;

    /**
     * 温度报警上限值（℃）
     */
    @TableField("temperature_max")
    private BigDecimal temperatureMax;

    /**
     * 温度采集时间（几点采集）
     */
    @TableField("temperature_collect_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime temperatureCollectTime;

    /**
     * 温度采集间隔时间（小时）
     */
    @TableField("temperature_interval_hours")
    private Integer temperatureIntervalHours;

    /**
     * 湿度报警上限值（%）
     */
    @TableField("humidity_max")
    private BigDecimal humidityMax;

    /**
     * 湿度采集时间（几点采集）
     */
    @TableField("humidity_collect_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime humidityCollectTime;

    /**
     * 湿度采集间隔时间（小时）
     */
    @TableField("humidity_interval_hours")
    private Integer humidityIntervalHours;

    /**
     * 气体浓度报警上限值（ppm）
     */
    @TableField("gas_max")
    private Integer gasMax;

    /**
     * 气体浓度采集时间（几点采集）
     */
    @TableField("gas_collect_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gasCollectTime;

    /**
     * 气体浓度采集间隔时间（小时）
     */
    @TableField("gas_interval_hours")
    private Integer gasIntervalHours;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}