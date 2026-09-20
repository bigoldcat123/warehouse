package com.example.demo.system.entity.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.demo.system.entity.PO.House;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TongFengDTO {
    private String tongfengLx;

    private String tongfengZt;

    private String tongfengTu;

    private String TFModeWin;

    private String TongFengSst;
    public static TongFengDTO fromHouse(House house) {
        return new TongFengDTO(
                house.getTongfengLx(),
                house.getTongfengZt(),
                house.getTongfengTu(),
                house.getTFModeWin(),
                house.getTongFengSst()
        );
    }
}
