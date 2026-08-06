package com.example.demo.system.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

///1:1:1$ 16.7$5#1:1:2$  8.7$5#1:1:3$  8.9$5#1:1:
@Data
@Getter
@Setter
@ToString
public class DataTempPoint {
    private String x;
    private String y;
    private String z;
    private String value;
    private String status;

}
