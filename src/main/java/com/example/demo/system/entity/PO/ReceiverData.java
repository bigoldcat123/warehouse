package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@Getter
@Setter
@TableName("receiver_data")
@Data
public class ReceiverData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  数据id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 仓房编号
     */
    @TableField("HouseNo")
    private Integer houseNo;

    /**
     * 采集时间
     */
    @TableField("TestDate")
    private LocalDateTime testDate;

    /**
     * 内部温度
     */
    @TableField("intemperature")
    private Float intemperature;

    /**
     * 内部湿度
     */
    @TableField("InHumidity")
    private Float inHumidity;

    /**
     * 外部温度
     */
    @TableField("OutTemperature")
    private Float outTemperature;

    /**
     * 外部湿度
     */
    @TableField("OutHumidity")
    private Float outHumidity;

    /**
     * 逗号分隔温度值序列，
     */
    @TableField("TemperatureSet")
    private String temperatureSet;

    /**
     * 水分
     */
    @TableField("GrainWater")
    private Float grainWater;


}
