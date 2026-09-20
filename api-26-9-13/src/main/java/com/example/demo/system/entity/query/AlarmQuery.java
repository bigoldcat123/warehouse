package com.example.demo.system.entity.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlarmQuery {
    public  static  AlarmQuery QUERY_BUFFER;
    public  static  List<String> LEVEL_BUFFER = new ArrayList<>();

    public  static final Integer LEVEL_COMMON = 1;
    public  static final Integer LEVEL_SEVERE = 2;

    public static final Integer TYPE_HOT = 1;
    public static final Integer TYPE_DISCONTINUITY = 2;
    public static final Integer TYPE_MILDEW = 3;
    public static final Integer TYPE_DEW = 4;
    public static final Integer TYPE_EMPTY = 5;
    public static final Integer TYPE_STANDBY = 6;

    List<String> type;
    List<String> level;
    List<Boolean> verify;
    String wareHouseID;
}
