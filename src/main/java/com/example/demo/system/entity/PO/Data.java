package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author czh
 * @since 2024-07-28
 */
@Getter
@Setter
@TableName("receiver_data")
public class Data implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("HouseNo")
    private String houseNo;

    @TableField("TestDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;

    @TableField("intemperature")
    private Float inTemperature;

    @TableField("InHumidity")
    private Float inHumidity;

    @TableField("OutTemperature")
    private Float outTemperature;

    @TableField("OutHumidity")
    private Float outHumidity;

    @TableField("TemperatureSet")
    private String temperatureSet;

    @TableField("GrainWater")
    private Float grainWater;


}
