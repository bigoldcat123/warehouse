package com.example.demo.common.tasks;

import com.example.demo.system.service.impl.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MqttTask {
    @Autowired
    MqttService mqttService;
    @Scheduled(fixedRate = 10000)
    public void task1() {
        mqttService.fetch();
    }
}
