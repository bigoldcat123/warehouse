package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库全景图中的仓房最新环境信息。
 */
@Data
public class WarehousePanoramaInfoDTO {

    private String houseNo;

    @JsonProperty("oAir")
    private Integer oAir;

    @JsonProperty("co2Air")
    private Integer co2Air;

    private String ph3;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testDate;
}
