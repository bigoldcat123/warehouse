package com.example.demo.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.mqtt.MqttDeviceInfoResponse;
import com.example.demo.common.mqtt.MqttExecutor;
import com.example.demo.common.mqtt.QueryParams;
import com.example.demo.system.entity.PO.GfKt;
import com.example.demo.system.entity.PO.House;
import com.example.demo.system.service.IGfKtService;
import com.example.demo.system.service.IHouseService;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MqttService {
    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    @Value("${czh.mqtt.server}")
    String server;
    @Value("${czh.mqtt.userName}")
    String userName;
    @Value("${czh.mqtt.password}")
    String password;


    MqttClient client;

    @Autowired
    IGfKtService gfKtService;
    @Autowired
    IHouseService houseService;

    static class MessageCallback implements MqttCallback {
        IHouseService houseService;

        public MessageCallback(IHouseService houseService) {
            this.houseService = houseService;
        }

        @Override
        public void connectionLost(Throwable cause) {
            cause.printStackTrace();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            MqttDeviceInfoResponse res = MqttDeviceInfoResponse.FromJson(new String(message.getPayload()));
            House house = houseService.getHouseByNo(res.getHouseNo_safely());
            if (house != null) {
                if (res.get_pv_elec_safely() != null) {
                    house.setPvElec(res.get_pv_elec_safely());
                }
                if (res.getAirHourElec_safely() != null) {
                    house.setAirHourElec(res.getAirHourElec_safely());
                }
                if (res.getMeterElec_safely() != null) {
                    house.setMeterElec(res.getMeterElec_safely());
                }
                houseService.updateById(house);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

    public void fetch() {
        if (client == null || !client.isConnected()) {
            init();
        }
        if (client.isConnected()) {
            subscribe();
            gfKtService.list().forEach(x -> {
                try {
                    log.info("fetch -> /publish/sinograin/devices/{}/command", x.getWareHouseNo());
                    client.publish("/publish/sinograin/devices/"+x.getWareHouseNo()+"/command",new MqttMessage(new QueryParams(x.getHouseNo(), 1).toBytes()));
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            });
        }


    }
    public void subscribe() {
        gfKtService.list().stream().map(x -> "/sinograin/devices/" + x.getWareHouseNo() +"/commandResponse").forEach(x -> {
            try {
                System.out.println(x);
                client.subscribe(x);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @PostConstruct
    public void init() {
        try {
            client = new MqttClient("tcp://" + server,"sinograin_kt_TJML_001abefg",new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());
            client.connect(connOpts);
            client.setCallback(new MessageCallback(houseService));
        } catch (MqttException e) {
            log.error("MQTT server not avaliable!");
            client = null;
        }
    }

}
