package com.example.demo.system.entity.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.demo.system.entity.PO.House;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Data
public class DataDetailDTO {

    private String wareHouseName;

    private String houseName;

    private String houseNo;

    private String breed;

    private String water;

    private String keeper;

    private String inTemperature;

    private String outTemperature;

    private String inHumidity;

    private String outHumidity;

    private String entryTime;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm")
    private LocalDateTime testTime;

    List<List<List<String>>> list = new ArrayList<>();

    public void setListAuto(List<Float> floatStream, House house) {
        Integer x = house.getX();
        Integer y = house.getY();
        Integer z = house.getZ();
        int num = 0;
        for (int i = 0; i < z; i++) {
            List<List<String>> map = new ArrayList<>();
            for (int j = 0; j < x; j++) {
                List<String> lie = new ArrayList<>();
                for (int k = 0; k < y; k++) {
                    lie.add(String.format("%.2f", floatStream.get(num++)));
                }
                map.add(lie);
            }
            list.add(map);
        }
    }
}
