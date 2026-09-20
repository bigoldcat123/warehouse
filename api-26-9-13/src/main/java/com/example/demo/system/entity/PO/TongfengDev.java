package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.service.IHouseService;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author czh
 * @since 2025-09-04
 */
@Getter
@Setter
@TableName("tongfeng_dev")
public class TongfengDev implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @TableField("WareHouseNo")
    private String wareHouseNo;

    @TableField("HouseNo")
    private String houseNo;

    /**
     * 1面风机数
     */
    @TableField("FengJiNum_1")
    private Integer fengjinum1;

    /**
     * 2面风机数
     */
    @TableField("FengJiNum_2")
    private Integer fengjinum2;

    /**
     * 风机参数
     */
    @TableField("DevParam")
    private String devParam;

    /**
     * =1：2面风机，=0:1面风机
     */
    @TableField("Side")
    private Boolean side;


    public House toHouse(IHouseService houseService) {

        return houseService.getHouseByNo(houseNo);
    }
}
