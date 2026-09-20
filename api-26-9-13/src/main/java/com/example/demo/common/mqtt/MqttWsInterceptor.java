package com.example.demo.common.mqtt;

import com.example.demo.security.JwtAuthorizationManager;
import com.example.demo.security.authentication.JwtAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class MqttWsInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(MqttWsInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            if (params.length >= 3) {
                String[] kv_warehouse_no = params[1].split("=");
                String[] kv_house_no = params[2].split("=");
                if (kv_warehouse_no.length == 2 && kv_house_no.length == 2) {
                    String ware_house_no = kv_warehouse_no[1].trim();
                    String house_no = kv_house_no[1].trim();
                    attributes.put("ware_house_no", ware_house_no);
                    attributes.put("house_no", house_no);
                    return true;
                }
            }
        }
        log.error("没有足够参数！");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("ws 参数验证成功");
    }
}
