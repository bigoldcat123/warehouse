package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author czh
 * @since 2025-10-10
 */
@Getter
@Setter
@TableName("nhlstt")
public class Nhlstt implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @TableField("HouseNo")
    private String houseNo;

    @TableField("WareHouseNo")
    private String wareHouseNo;

    /**
     * 仓湿
     */
    @TableField("InH")
    private Double inH;

    /**
     * 仓温
     */
    @TableField("`InT`")
    private Double inT;

    /**
     * 粮温
     */
    @TableField("GT")
    private Double gt;

    /**
     * 开机温度
     */
    @TableField("HiTemp")
    private Double hiTemp;

    /**
     * 开机温度
     */
    @TableField("LoTemp")
    private Double loTemp;

    /**
     * 检测时间
     */
    @TableField("tmTest")
    private LocalDateTime tmTest;

    /**
     * 1：开机；0开机
     */
    @TableField("Run")
    private Boolean run;


}
