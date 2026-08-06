package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 粮房温度记录DTO
 * 包含检测日期和对应的温度数组
 */
@Data
public class HouseTempRecordDTO {
    
    /**
     * 检测日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;
    
    /**
     * 温度数组（一维数组，按层→行→列顺序排列）
     */
    private List<String> temp;
}
