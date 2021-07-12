package com.example.jjmessageservice.controller;

import com.example.jjmessageservice.dto.ChatInfoDto;
import com.example.jjmessageservice.model.ChatMessage;
import com.example.jjmessageservice.model.ChatNotification;
import com.example.jjmessageservice.service.ChatMessageService;
import com.example.jjmessageservice.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        System.out.println(chatMessage);

        Optional<String> chatId = chatRoomService
                .getChatId(chatMessage, true);
        chatMessage.setChatId(chatId.get());
        ChatMessage saved = chatMessageService.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),"/queue/messages",
                new ChatNotification(
                        saved.getId(),
                        saved.getContent(),
                        saved.getSenderId(),
                        saved.getSenderName(),
                        chatId.get(),
                        chatMessageService.countNewMessages(saved.getSenderId(), chatMessage.getRecipientId())));
    }

    @GetMapping("/api/messages/{senderId}/{recipientId}/count")
    public ResponseEntity<Long> countNewMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId) {

        return ResponseEntity.ok(chatMessageService.countNewMessages(senderId, recipientId));
    }

    @GetMapping("/api/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages (@PathVariable String senderId,
                                                               @PathVariable String recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }

    @PutMapping("/api/readMessage/{id}")
    public void readMessage (@PathVariable String id) {
        chatMessageService.readMessageById(id);
    }

    @GetMapping("/api/chats/{senderId}")
    public ResponseEntity<List<ChatInfoDto>> findChatsRecipientsBySenderId (@PathVariable String senderId) {
        return ResponseEntity.ok(chatRoomService.findAllRecipientsBySenderId(senderId));
    }
}
