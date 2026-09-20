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
 * 熏蒸记录。
 */
@Data
@TableName("xunzheng")
public class Xunzheng implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 仓房编号。
     */
    @TableField("HouseNo")
    private String houseNo;

    /**
     * 开始熏蒸日期时间。
     */
    @TableField("start_DT")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDt;

    /**
     * 结束熏蒸日期时间。
     */
    @TableField("stop_DT")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopDt;

    /**
     * 此条记录修改时间。
     */
    @TableField("set_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime setDatetime;
}
