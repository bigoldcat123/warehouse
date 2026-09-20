package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓房测点定义。
 *
 * <p>Length 和 Width 共同描述传感器的分层排列。例如 Length=5、Width=3，
 * 表示共有 3 层，每层布置 5 个传感器，总计 15 个传感器。</p>
 */
@Data
@TableName("pointdefine")
public class PointDefine implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓房名称。
     */
    @TableField("HouseName")
    private String houseName;

    /**
     * 仓房编号。
     */
    @TableId(value = "HouseNo", type = IdType.INPUT)
    private String houseNo;

    /**
     * 每层布置的传感器数量（串数）。
     * 值为 1 表示每层 1 串，值为 2 表示每层 2 串，以此类推。
     */
    @TableField("Length")
    private String length;

    /**
     * 传感器从上到下的层数。
     * 每一层包含 Length 指定数量的传感器。
     */
    @TableField("Width")
    private String width;

    /**
     * 高度，字段名沿用数据库的 Hight。
     */
    @TableField("Hight")
    private String hight;

    /**
     * 是否允许检测。
     */
    @TableField("bMayTest")
    private String bMayTest;
}
