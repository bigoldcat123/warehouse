package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓房采集数据。
 */
@Data
@TableName("receiver_data")
public class ReceiverData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID。
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 仓房编号。
     */
    @TableField("HouseNo")
    private String houseNo;

    /**
     * 采集时间。
     */
    @TableField("TestDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;

    /**
     * 仓内温度。
     */
    @TableField("intemperature")
    private Double inTemperature;

    /**
     * 仓内湿度。
     */
    @TableField("InHumidity")
    private Double inHumidity;

    /**
     * 仓外温度。
     */
    @TableField("OutTemperature")
    private Double outTemperature;

    /**
     * 仓外湿度。
     */
    @TableField("OutHumidity")
    private Double outHumidity;

    /**
     * PH3 浓度值序列，使用逗号分隔。
     */
    @TableField("TemperatureSet")
    private String temperatureSet;

    /**
     * 温度值序列。
     */
    @TableField("TempData")
    private String tempData;

    /**
     * 湿度值序列。
     */
    @TableField("HumiData")
    private String humiData;

    /**
     * 温湿度及水分数据采集时间。
     */
    @TableField("TestDate_THW")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDateThw;

    /**
     * 氧气浓度。
     */
    @TableField("OAir")
    private Integer oAir;

    /**
     * 二氧化碳浓度。
     */
    @TableField("CO2Air")
    private Integer co2Air;

    /**
     * 粮食水分。
     */
    @TableField("GrainWater")
    private Double grainWater;

    /**
     * 库区编号。
     */
    @TableField("WareHouseNo")
    private String wareHouseNo;
}
