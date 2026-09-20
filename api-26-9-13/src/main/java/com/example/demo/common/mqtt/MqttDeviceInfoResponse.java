package com.example.demo.common.mqtt;


import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class MqttDeviceInfoResponse {
    private String mid;
    private List<Device> devices;

    public String getHouseNo_safely() {
        if (devices == null || devices.isEmpty()) {
            return "";
        }else {
            return devices.getFirst().getData().warehouse_code + "";
        }
    }
    public Integer get_pv_elec_safely() {
        if (devices == null || devices.isEmpty()) {
            return null;
        }else {
            return devices.getFirst().getData().PV_elec;
        }
    }
    public Double getAirHourElec_safely() {
        if (devices == null || devices.isEmpty()) {
            return null;
        }else {
            if (devices.getFirst().getData().air_data == null || devices.getFirst().getData().air_data.isEmpty()) {
                return null;
            }else {
                return devices.getFirst().getData().air_data.getFirst().getAir_hour_elec();
            }
        }
    }
    public Double getMeterElec_safely() {
        if (devices == null || devices.isEmpty()) {
            return null;
        }else {
            if (devices.getFirst().getData().meter_data == null || devices.getFirst().getData().meter_data.isEmpty()) {
                return null;
            }else {
                return devices.getFirst().getData().meter_data.getFirst().getMeter_elec();
            }
        }
    }
    // getters and setters

    @lombok.Data
    public static class Device {
        private String serciceId;
        private Data data;

        // getters and setters
    }

    @lombok.Data
    public static class Data {
        private String warehouse_code;
        private String warehouse_name;
        private Integer PV_elec;
        private List<AirData> air_data;
        private List<MeterData> meter_data;
        private String eventTime;

    }

    @lombok.Data
    public static class AirData {
        private String air_assetCode;
        private double air_hour_elec;

    }

    @lombok.Data
    public static class MeterData {
        private String meter_name;
        private String meter_addr;
        private String meter_installAddr;
        private double meter_elec;

    }
    public static MqttDeviceInfoResponse FromJson(String json_str)  {
        return JSON.parseObject(replace(json_str), MqttDeviceInfoResponse.class);
    }
    public static String replace(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // 正则表达式：匹配 "warehouse_code": 后面的数字（不带引号的）
        // 匹配模式："warehouse_code":\s*(\d+)
        String regex = "(\"warehouse_code\"\\s*:\\s*)(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        // 替换：在数字两边加上双引号
        String result = matcher.replaceAll("$1\"$2\"");

        return result;
    }
}