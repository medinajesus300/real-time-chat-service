package com.medina.chat_service.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks user presence and provides presence list to subscribers.
 */
@Component
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public PresenceEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle new connections: add user and broadcast updated presence.
     */
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            onlineUsers.add(user.getName());
            messagingTemplate.convertAndSend("/topic/presence", onlineUsers);
        }
    }

    /**
     * Handle disconnections: remove user and broadcast updated presence.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            onlineUsers.remove(user.getName());
            messagingTemplate.convertAndSend("/topic/presence", onlineUsers);
        }
    }

    /**
     * Provide current presence list on demand.
     * Invoked when client sends to /app/presence.
     */
    @MessageMapping("/presence")
    @SendTo("/topic/presence")
    public Set<String> sendPresence() {
        return onlineUsers;
    }



}
