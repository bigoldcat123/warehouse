package com.example.demo.common.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;

@Component
public class MqttWsHandler implements WebSocketHandler {
    @Value("${czh.mqtt.server}")
    String server;
    @Value("${czh.mqtt.userName}")
    String userName;
    @Value("${czh.mqtt.password}")
    String password;

    static HashMap<Object,MqttExecutor> THREADS = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String wareHouseNo = (String) session.getAttributes().get("ware_house_no");
        String house_no = (String) session.getAttributes().get("house_no");
        MqttExecutor executor = new MqttExecutor("tcp://" + server,"sinograin_kt_TJML_001",userName,password,30000);
        executor.run(wareHouseNo,house_no,(message) -> {
            try {
                session.sendMessage(new TextMessage(MqttDeviceInfoResponse.replace(new String(message.getPayload()))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        THREADS.put(session.getId(), executor);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println(closeStatus);
        MqttExecutor thread = THREADS.remove(session.getId());
        thread.interrupt();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
