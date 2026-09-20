package com.example.demo.common.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.function.Consumer;

public class MqttMessageHandler implements MqttCallback {

    Consumer<MqttMessage> callback;

    public MqttMessageHandler(Consumer<MqttMessage> callback) {
        this.callback = callback;
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.callback.accept(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
