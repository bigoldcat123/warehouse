package com.example.demo.common.mqtt;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueryParams {
    // actually house_code
    String warehouse_code;
    // {year:4}{month:2}{day:2}{hour:2}
    @JsonFormat(pattern = "yyyyMMddHH", timezone = "GMT+8")
    LocalDateTime startTime;
    @JsonFormat(pattern = "yyyyMMddHH", timezone = "GMT+8")
    LocalDateTime endTime;

    public QueryParams(String warehouse_code,int house_diff) {
        this.warehouse_code = warehouse_code;
        this.startTime = LocalDateTime.now().minusHours(house_diff);
        this.endTime = LocalDateTime.now();
    }

    public byte[] toBytes() {
        return JSON.toJSONBytes(this);
    }
}
