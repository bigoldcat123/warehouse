package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataDTO {
    private Integer id;
    private String houseNo;
    private String houseName;

    private String house_type;

    /**
     * 各层 PH3 最大值，格式保持为“值 |值 |”。
     */
    private String layerMax;

    /**
     * 各层 PH3 最小值，格式保持为“值 |值 |”。
     */
    private String layerMin;

    /**
     * 各层 PH3 平均值，格式保持为“值 |值 |”。
     */
    private String layerAvg;

    /**
     * 仓间 PH3，取 TemperatureSet 的最后一个数字。
     */
    private String housePh3;

    /**
     * 仓间氧气浓度。
     */
    @JsonProperty("oAir")
    private Integer oAir;

    /**
     * 仓间二氧化碳浓度。
     */
    @JsonProperty("co2Air")
    private Integer co2Air;

    @JsonFormat(pattern = "yy-MM-dd")
    private LocalDateTime testDate;
}
