package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("alarm")
public class Alarm implements Serializable {
    public static final String LEVEL_COMMON = "一般";
    public static final String LEVEL_SERIOUS = "严重";

    public static final String TYPE_HOT = "发热";
    public static final String TYPE_INCONSISTENT = "不连续";
    public static final String TYPE_MOULD = "霉变趋势";
    public static final String TYPE_DEW = "结露趋势";
    public static final String TYPE_EMPTY= "空仓";
    public static final String TYPE_SPARE = "通风";

    public static final List<String> ARG_DEW_AND_MOULD = List.of(TYPE_DEW,TYPE_MOULD);
    public static final List<String> ARG_EXCEPTION = List.of(TYPE_INCONSISTENT,TYPE_EMPTY,TYPE_SPARE);

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属仓房
     */

    @TableField("houseNo")
    private String houseNo;

    /**
     * 报警位置
     */
    @TableField("alert_pos")
    private String alertPos;

    /**
     * 报警类型
     */
    @TableField("alert_type")
    private String alertType;

    /**
     * 报警级别
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 报警时间
     */
    @TableField("alert_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alertTime;

    /**
     * 处理意见
     */
    @TableField("handle")
    private String handle;

    /**
     * 是否核准 0: no 1: yes
     */
    @TableField("is_verify")
    private Boolean isVerify;

    @TableField("yuntu")
    private String yuntu;

}
