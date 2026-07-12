package com.example.demo.common.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class MqttExecutor {
    private static final Logger log = LoggerFactory.getLogger(MqttExecutor.class);
    MqttClient client;
    Thread thread;
    Integer interval;

    public MqttExecutor(String broker, String clientId, String userName, String password,Integer interval) throws MqttException {
        MqttClient mqttClient = new MqttClient(broker,clientId,new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(userName);
        this.interval = interval;
        connOpts.setPassword(password.toCharArray());
        mqttClient.connect(connOpts);
        client = mqttClient;
    }
    public void run(String ware_house_no,String house_no, Consumer<MqttMessage> callback) throws MqttException {
        client.setCallback(new MqttMessageHandler(callback));
        client.subscribe("/sinograin/devices/" + ware_house_no +"/commandResponse");
//        System.out.println("/sinograin/devices/" + ware_house_no +"/commandResponse");
//        System.out.println("/publish/sinograin/devices/"+ware_house_no+"/command");

        thread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    client.publish("/publish/sinograin/devices/"+ware_house_no+"/command",new MqttMessage(new QueryParams(house_no, 1).toBytes()));
                    Thread.sleep(interval);
                } catch (InterruptedException | MqttException e) {
                    log.info("yes 我退出啦");
                    break;
                }
            }
        });
        thread.start();

    }
    public void interrupt() {
        this.thread.interrupt();
        try {
            this.client.disconnect();
            this.client.close(true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        log.info("mqtt 接受线程退出成功");
    }

}
