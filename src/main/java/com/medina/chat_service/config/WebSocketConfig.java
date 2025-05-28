package com.medina.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP over WebSocket endpoints and message broker.
 * Enables automatic registration of SimpMessagingTemplate bean.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")                  // WebSocket handshake endpoint
                .setAllowedOriginPatterns("*")       // Allow all origins; tighten in prod
                .withSockJS();                         // Fallback options for non-WebSocket clients
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .setApplicationDestinationPrefixes("/app")  // Client messages prefix
                .enableSimpleBroker("/topic");             // In-memory broker for pub/sub
    }


}
