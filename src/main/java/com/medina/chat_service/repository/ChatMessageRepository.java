package com.medina.chat_service.repository;

import com.medina.chat_service.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieve the earliest 100 messages for history display.
     * @return List of ChatMessage ordered by timestamp ascending
     */
    List<ChatMessage> findTop100ByOrderByTimestampAsc();

}
