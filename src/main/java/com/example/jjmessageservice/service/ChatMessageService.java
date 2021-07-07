package com.example.jjmessageservice.service;

import com.example.jjmessageservice.exception.ResourceNotFoundException;
import com.example.jjmessageservice.model.ChatMessage;
import com.example.jjmessageservice.model.MessageStatus;
import com.example.jjmessageservice.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    private final ChatRoomService chatRoomService;

    @Autowired
    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatRoomService chatRoomService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomService = chatRoomService;
    }

    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setStatus(MessageStatus.RECEIVED);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    public long countNewMessages(String senderId, String recipientId) {
        return chatMessageRepository.countBySenderIdAndRecipientIdAndStatus(
                senderId, recipientId, MessageStatus.RECEIVED);
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.findChatIdBySenderAndRecipient(senderId, recipientId);

        var messages = chatId.map(cId -> chatMessageRepository.findByChatId(cId)).orElse(new ArrayList<>());

        if(messages.size() > 0) {
            messages.forEach(message -> {
                message.setStatus(MessageStatus.DELIVERED);
                chatMessageRepository.save(message);
            });
        }

        messages.sort(Comparator.comparing(ChatMessage::getTimestamp));

        return messages;
    }

    public void readMessageById(String id) {
        ChatMessage message = chatMessageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("can't find message (" + id + ")"));
        message.setStatus(MessageStatus.DELIVERED);
        chatMessageRepository.save(message);
    }
}