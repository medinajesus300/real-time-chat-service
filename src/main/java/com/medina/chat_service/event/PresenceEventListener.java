package com.medina.chat_service.event;

import com.medina.chat_service.dto.ChatMessageDto;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks user presence and broadcasts join/leave notices as chat messages.
 */
@Controller
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public PresenceEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle WebSocket CONNECT: add user, broadcast presence list,
     * and send a “User X joined” system message.
     */
    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            String username = user.getName();
            onlineUsers.add(username);

            // 1) Broadcast updated presence list
            messagingTemplate.convertAndSend("/topic/presence", onlineUsers);

            // 2) Send a “user joined” system message
            ChatMessageDto notice = new ChatMessageDto(
                    "System",
                    username + " has joined the chat",
                    Instant.now()
            );
            messagingTemplate.convertAndSend("/topic/messages", notice);
        }
    }

    /**
     * Handle WebSocket DISCONNECT: remove user, broadcast presence list,
     * and send a “User X left” system message.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            String username = user.getName();
            onlineUsers.remove(username);

            // 1) Broadcast updated presence list
            messagingTemplate.convertAndSend("/topic/presence", onlineUsers);

            // 2) Send a “user left” system message
            ChatMessageDto notice = new ChatMessageDto(
                    "System",
                    username + " has left the chat",
                    Instant.now()
            );
            messagingTemplate.convertAndSend("/topic/messages", notice);
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
