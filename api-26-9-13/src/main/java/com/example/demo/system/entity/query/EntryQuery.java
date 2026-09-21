package com.example.demo.system.entity.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntryQuery {
    String warehouseId;
    Integer houseId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime from;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime to;
}
