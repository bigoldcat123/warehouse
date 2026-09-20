package com.example.demo.config;

import com.example.demo.common.mqtt.MqttWsHandler;
import com.example.demo.common.mqtt.MqttWsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@EnableWebSocket
@Configuration
public class WsConfiguration implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WsConfiguration.class);
    @Autowired
    MqttWsHandler mqttWsHandler;

    @Autowired
    WsAuthInterceptor authInterceptor;
    @Autowired
    MqttWsInterceptor mqttWsInterceptor;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mqttWsHandler,"/ws").addInterceptors(authInterceptor,mqttWsInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
