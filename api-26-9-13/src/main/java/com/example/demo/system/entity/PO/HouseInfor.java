package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓房粮情信息。
 */
@Data
@TableName("houseinfor")
public class HouseInfor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓房编号。
     */
    @TableField("HouseNo")
    private String houseNo;

    /**
     * 粮食数量。
     */
    @TableField("GrainCount")
    private Integer grainCount;

    /**
     * 保管员姓名。
     */
    @TableField("KeeperName")
    private String keeperName;

    /**
     * 粮食品种编码。
     */
    @TableField("GrainCode")
    private String grainCode;

    /**
     * 粮食品种名称。
     */
    @TableField("GrainName")
    private String grainName;

    /**
     * 入库时间。
     */
    @TableField("DateOfIn")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateOfIn;

    /**
     * 出库时间。
     */
    @TableField("DateOfOut")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateOfOut;

    /**
     * 检测时间。
     */
    @TableField("TestDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;

    /**
     * 粮食水分。
     */
    @TableField("GrainWater")
    private Double grainWater;
}
