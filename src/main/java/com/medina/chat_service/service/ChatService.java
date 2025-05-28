package com.medina.chat_service.service;

import com.medina.chat_service.model.ChatMessage;
import com.medina.chat_service.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;


@Service
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository messageRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Persist and broadcast a new chat message.
     * @param senderUsername username of the message sender
     * @param content the message text
     * @return the saved ChatMessage entity
     */
    public ChatMessage sendMessage(String senderUsername, String content) {
        ChatMessage msg = new ChatMessage();

        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        ChatMessage saved = messageRepository.save(msg);
        messagingTemplate.convertAndSend("/topic/messages", saved);
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
