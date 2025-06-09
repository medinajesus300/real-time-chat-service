package com.medina.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.security.Principal;

/**
 * Configures STOMP over WebSocket endpoints, message broker,
 * and assigns a Principal based on CONNECT header for presence tracking.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")                  // WebSocket handshake endpoint
                .setAllowedOriginPatterns("*")       // Allow all origins; tighten in prod
                .withSockJS();                        // Fallback for non-WebSocket browsers
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix for messages sent from client to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app")
                // Prefix for user‑specific destinations (convertAndSendToUser)
                .setUserDestinationPrefix("/user");

        // In‑memory simple broker handles broadcasts (/topic) and private queues (/queue)
        config.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String username = accessor.getFirstNativeHeader("username");
                    if (username != null && !username.isBlank()) {
                        Principal user = new UsernamePasswordAuthenticationToken(username, null);
                        accessor.setUser(user);
                    }
                }
                return message;
            }
        });
    }
}
