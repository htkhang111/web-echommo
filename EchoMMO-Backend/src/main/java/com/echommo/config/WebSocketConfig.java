package com.echommo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint /ws để Frontend connect vào
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép mọi nguồn (tránh lỗi CORS)
                .withSockJS(); // Kích hoạt SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Các destination bắt đầu bằng /app sẽ được route tới @MessageMapping trong Controller
        registry.setApplicationDestinationPrefixes("/app");

        // Các destination bắt đầu bằng /topic sẽ được route tới Broker (gửi về client)
        registry.enableSimpleBroker("/topic");
    }
}