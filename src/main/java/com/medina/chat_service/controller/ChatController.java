package com.medina.chat_service.controller;

import com.medina.chat_service.dto.ChatMessageDto;
import com.medina.chat_service.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket controller for chat messages.
 */
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;   // <‑‑ ADDED

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle incoming chat messages: persist and broadcast.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto dto) {
        chatService.sendMessage(dto.getSender(), dto.getContent());
    }

    /**
     * Fetch recent chat history on subscription.
     * History is sent privately to the requesting user to prevent duplicates
     * in other clients.
     */
    @MessageMapping("/chat.history")
    public void fetchHistory(Principal principal) {           // <‑‑ CHANGED signature
        List<ChatMessageDto> history = chatService.fetchHistory().stream()
                .map(msg -> new ChatMessageDto(
                        msg.getSender().getUsername(),
                        msg.getContent(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());

        // <‑‑ SEND PRIVATELY ‑‑>
        messagingTemplate.convertAndSendToUser(
                principal.getName(),           // destination user
                "/queue/history",            // client subscribes to /user/queue/history
                history);
    }
}
