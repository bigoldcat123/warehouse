package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.R;
import com.example.demo.common.mqtt.MqttExecutor;
import com.example.demo.system.entity.DTO.GfKTDTO;
import com.example.demo.system.entity.PO.GfKt;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.service.IGfKtService;
import com.example.demo.system.service.IHouseService;
import jakarta.websocket.server.PathParam;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/gfkt")
public class GfController {
    @Autowired
    IGfKtService gfKtService;
    @Autowired
    IHouseService houseService;
    @GetMapping()
    R getAll() {

        List<String> houseNoes = gfKtService.list().stream().map(GfKt::getHouseNo).toList();
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("HouseNo", houseNoes);
        for (String houseNoe : houseNoes) {
            System.out.println(houseService.getHouseByNo(houseNoe));
        }
        List<GfKTDTO> houses = houseService.list(queryWrapper).stream().map(House::into_gf_kt).toList();
        return R.ok(houses);
    }
    @GetMapping("/{houseNo}")
    R getOneByHouseNo(@PathVariable("houseNo") String houseNo) {
        House house = houseService.getHouseByNo(houseNo);
        GfKTDTO gfKTDTO = house.into_gf_kt();
        return R.ok(gfKTDTO);
    }
}
