package com.example.demo.config;

import com.example.demo.security.JwtAuthorizationManager;
import com.example.demo.security.authentication.JwtAuthorization;
import jakarta.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
public class WsAuthInterceptor implements HandshakeInterceptor {
    @Autowired
    JwtAuthorizationManager jwtAuthorizationManager;
    private static final Logger log = LoggerFactory.getLogger(WsAuthInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // TODO 验证WS 是否可靠。。。
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            if (params.length > 1) {
                String[] kv = params[0].split("=");
                if (kv.length == 2) {
                    String token = kv[1];
                    JwtAuthorization jwtAuthorization = new JwtAuthorization(token);
                    try {
                        jwtAuthorizationManager.verify(() -> jwtAuthorization, null);
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        attributes.put("id", authentication.getPrincipal());
                        return true;
                    } catch (Exception e) {
                        log.error("Authentication failed", e);
                        return false;
                    }
                }
            }
        }
        log.error("WebSocket 需要 token ！");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("ws 验证成功");
    }
}
