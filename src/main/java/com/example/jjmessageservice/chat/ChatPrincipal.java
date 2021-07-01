package com.example.jjmessageservice.chat;

import java.security.Principal;

/**
 * Custom Principal class which is used for anonymous user sessions in Websocket connection
 */
public class ChatPrincipal implements Principal {
    private final String username;

    public ChatPrincipal(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }
}