package com.example.demo.system.entity.PO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 粮库名称信息。
 */
@Data
@TableName("storename")
public class StoreName implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 粮库编号。
     */
    @TableId(value = "StoreNo", type = IdType.INPUT)
    @JsonProperty("warehouseNo")
    private String storeNo;

    /**
     * 粮库名称。
     */
    @TableField("StoreName")
    @JsonProperty("warehouseName")
    private String storeName;
}
