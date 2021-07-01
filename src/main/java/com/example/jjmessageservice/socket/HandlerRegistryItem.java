package com.example.jjmessageservice.socket;

import lombok.Data;

@Data
public class HandlerRegistryItem<T> {
    private final String type;
    private final Class<T> clazz;
    private final MessageHandler<T> handler;


    public HandlerRegistryItem(String type, Class<T> clazz, MessageHandler<T> handler) {
        this.type = type;
        this.clazz = clazz;
        this.handler = handler;
    }
}
