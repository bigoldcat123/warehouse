package com.example.demo.system.entity.DTO;

import lombok.Data;

@Data
public class AlarmArgDTO {
    public AlarmArgDTO(String wareHouseNO, String wareHouseName) {
        this.wareHouseNO = wareHouseNO;
        this.wareHouseName = wareHouseName;
    }

    String wareHouseNO;
    String wareHouseName;
    int common;
    int serious;
    int hot;
    int dewAndMould;
    int exception;
    public void addCommon() {
        this.common++;
    }
    public void addSerious() {
        this.serious++;
    }
    public void addHot() {
        this.hot++;
    }
    public void addDewAndMould() {
        this.dewAndMould++;
    }
    public void addException() {
        this.exception++;
    }

}
