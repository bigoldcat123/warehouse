package com.example.demo.system.entity.DTO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.system.entity.PO.Nhlstt;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

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
public class NhlsttHouseNameDTO  extends  Nhlstt {

    String houseName;

}
