package com.example.demo.common;

public class Utils {
    public static boolean is_valid_temperature(float n) {
        return n >= -40.0 && n <= 80.0;
    }

    public static boolean is_valid_humidity(float n) {
        return n >= -0 && n <= 100.0;
    }
}
