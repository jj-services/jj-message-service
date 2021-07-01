package com.example.jjmessageservice.chat;

import lombok.Data;

@Data
public class ChatMessage {
    private String from;
    private String to;
    private String message;
}