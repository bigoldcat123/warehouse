package com.example.demo.system.entity.DTO;

import com.example.demo.system.entity.PO.Alarm;
import lombok.Data;

@Data
public class AlarmDTO  {

    public AlarmDTO(Alarm alarm) {

        this.alarm = alarm;
    }

    Alarm alarm;
    String yuntu;
}
