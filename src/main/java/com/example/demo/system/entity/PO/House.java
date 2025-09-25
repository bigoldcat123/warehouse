package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.system.entity.DTO.HouseDTO;
import com.example.demo.system.service.IEntryService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;

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
@TableName("house")
@ToString
public class House implements Serializable {



    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 仓房编号
     */
    @TableField("HouseNo")
    private String houseNo;

    /**
     * 所属仓库
     */
    @TableField("warehouseID")
    private Integer warehouseID;

    /**
     * 仓房名
     */
    @TableField("house_name")
    private String houseName;

    /**
     * 仓房地址
     */
    @TableField("house_addr")
    private String houseAddr;

    /**
     * 平房仓、筒仓选择一个
     */
    @TableField("house_type")
    private String houseType;

    /**
     * 层数
     */
    @TableField("z_num")
    private Integer z;

    /**
     * 行数
     */
    @TableField("x_num")
    private Integer x;

    /**
     * 列数
     */
    @TableField("y_num")
    private Integer y;

    @TableField("yuntu")
    private String yuntu;
    @TableField("quxian")
    private String quxian;

    @TableField("threeD")
    private String threeD;
    @TableField("ValeWin")
    private String valeWin;

    @TableField("tongfeng_lx")
    private String tongfengLx;

    @TableField("tongfeng_zt")
    private String tongfengZt;

    @TableField("tongfeng_tu")
    private String tongfengTu;

    @TableField("TFModeWin")
    private String TFModeWin;

    @TableField("TFMODESST")
    private String tfModSst;

    @TableField("TongFeng_sst")
    private String TongFengSst;
    public HouseDTO toDTO(IEntryService entry_service) {
        HouseDTO dto = new HouseDTO();
        BeanUtils.copyProperties(this, dto);
        QueryWrapper<Entry> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("entry_time").last("limit 1");
        queryWrapper.eq("houseID",this.id);
        Entry entry = entry_service.getOne(queryWrapper);
        if (entry != null) {
            dto.setBreed(entry.getBreed());
            dto.setEntryTime(entry.getEntryTime());
            dto.setWater(entry.getWater());
        }

        return dto;
    }
}
