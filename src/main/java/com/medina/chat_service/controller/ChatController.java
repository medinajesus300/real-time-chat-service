package com.medina.chat_service.controller;

import com.medina.chat_service.dto.ChatMessageDto;
import com.medina.chat_service.model.ChatMessage;
import com.medina.chat_service.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket controller for chat messages.
 */
@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handle incoming chat messages: persist and broadcast.
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessageDto sendMessage(@Payload ChatMessageDto dto) {
        ChatMessage saved = chatService.sendMessage(dto.getSender(), dto.getContent());
        return new ChatMessageDto(
                saved.getSender().getUsername(),
                saved.getContent(),
                saved.getTimestamp()
        );
    }

    /**
     * Fetch recent chat history on subscription.
     */
    @MessageMapping("/chat.history")
    @SendTo("/topic/messages")
    public List<ChatMessageDto> fetchHistory() {
        return chatService.fetchHistory().stream()
                .map(msg -> new ChatMessageDto(
                        msg.getSender().getUsername(),
                        msg.getContent(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }


}

