package com.example.demo.system.entity.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataDetailDTO {

    private String wareHouseName;

    private String houseName;

    private String houseNo;

    private String grainName;

    private Double grainWater;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateOfIn;

    private String keeperName;

    private String housePh3;

    @JsonProperty("oAir")
    private Integer oAir;

    @JsonProperty("co2Air")
    private Integer co2Air;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testTime;

    private List<List<Double>> ph3Matrix = new ArrayList<>();

    private List<List<Double>> temperatureMatrix = new ArrayList<>();
}
