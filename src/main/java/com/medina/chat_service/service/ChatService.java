package com.medina.chat_service.service;

import com.medina.chat_service.dto.ChatMessageDto;
import com.medina.chat_service.model.ChatMessage;
import com.medina.chat_service.model.User;
import com.medina.chat_service.repository.ChatMessageRepository;
import com.medina.chat_service.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for handling chat messages: persistence and broadcasting.
 */
@Service
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository messageRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Persist and broadcast a new chat message.
     * @param senderUsername username of the message sender
     * @param content the message text
     * @return the saved ChatMessage entity
     */
    public ChatMessage sendMessage(String senderUsername, String content) {
        if (senderUsername == null || senderUsername.isEmpty()) {
            throw new IllegalArgumentException("Sender username must not be null or empty");
        }
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + senderUsername));
        ChatMessage msg = new ChatMessage();
        msg.setSender(sender);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        ChatMessage saved = messageRepository.save(msg);
        ChatMessageDto broadcastDto = new ChatMessageDto(
                saved.getSender().getUsername(),
                saved.getContent(),
                saved.getTimestamp());
        messagingTemplate.convertAndSend("/topic/messages", broadcastDto);
        return saved;
    }

    /**
     * Fetch the latest 100 messages ordered by timestamp.
     * @return list of ChatMessage entities
     */
    public List<ChatMessage> fetchHistory() {
        return messageRepository.findTop100ByOrderByTimestampAsc();
    }

}
