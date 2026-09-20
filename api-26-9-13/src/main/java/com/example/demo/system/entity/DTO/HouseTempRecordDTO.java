package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 粮房温度记录DTO
 * 包含检测日期和对应的单个温度值
 */
@Data
public class HouseTempRecordDTO {
    
    /**
     * 检测日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;
    
    /**
     * 温度值（单个点）
     */
    private Float temp;
}
