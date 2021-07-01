package com.example.jjmessageservice.chat;

import com.example.jjmessageservice.socket.HandlerRegistry;
import com.example.jjmessageservice.socket.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new HashMap<>();
    private final HandlerRegistry handlerRegistry;

    public static final String MESSAGE_TOPIC = "/chat/message";
    private static final String USERS_TOPIC = "/chat/users";
    private static final String USERNAME_TOPIC = "/chat/username";

    public ChatHandler() {
        handlerRegistry = new HandlerRegistry()
                .registerHandler(MESSAGE_TOPIC, ChatMessage.class, this::handleChatMessage);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        if (session.getPrincipal() != null) {
            log.info("Received message {} from {} with sessionId {}", message, session.getPrincipal().getName(), message);
            handlerRegistry.handle(session, message.getPayload());
        }
    }

    public void handleChatMessage(WebSocketSession session, ChatMessage chatMessage) throws IOException {
        final WebSocketSession toSession = sessions.get(chatMessage.getTo());
        if (toSession == null) {
            log.info("Received chatMessage for user {} but is not connected", chatMessage.getTo());
        } else {
            final String message = new Message<>(MESSAGE_TOPIC, chatMessage).toJson();
            toSession.sendMessage(new TextMessage(message));
        }
    }


    private void notifyUsers() throws IOException {
        final String message = new Message<>(USERS_TOPIC, sessions.keySet()).toJson();

        for (WebSocketSession session : sessions.values()) {
            final TextMessage textMessage = new TextMessage(message);
            session.sendMessage(textMessage);
        }
    }

    private void notifyUsername(WebSocketSession session, String username) throws IOException {
        final String message = new Message<>(USERNAME_TOPIC, username).toJson();
        final TextMessage textMessage = new TextMessage(message);

        session.sendMessage(textMessage);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        log.info(String.format("WebSocket connection established for sessionID %s", session.getId()));
        if (session.getPrincipal() != null) {
            final String username = session.getPrincipal().getName();
            sessions.put(username, session);
            notifyUsername(session, username);
            notifyUsers();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        log.info(String.format("WebSocket connection closed for sessionID %s", session.getId()));
        if (session.getPrincipal() != null) {
            sessions.remove(session.getPrincipal().getName());
            notifyUsers();
        }
    }
}