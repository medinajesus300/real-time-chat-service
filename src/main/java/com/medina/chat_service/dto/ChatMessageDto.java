package com.medina.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String sender;
    private String content;
    private Instant timestamp;


}

