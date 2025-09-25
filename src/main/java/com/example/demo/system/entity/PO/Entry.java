package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 
 * </p>
 *
 * @author czh
 * @since 2024-07-27
 */
@Data
@TableName("_entry")
public class Entry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属仓房
     */
    @TableField("houseID")
    private Integer houseID;

    /**
     * 品种
     */
    @TableField("breed")
    private String breed;

    /**
     * 入库时间
     */
    @TableField("entry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime entryTime;

    /**
     * GrainWater
     */
    @TableField("water")
    private Float water;

    @TableField("stockman")
    private Integer stockman;

    @TableField("entry_userid")
    private Integer entryUserId;
}
