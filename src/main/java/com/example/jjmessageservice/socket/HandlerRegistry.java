package com.example.jjmessageservice.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class HandlerRegistry {
    private final Map<String, HandlerRegistryItem<?>> handlers = new HashMap<>();

    public <T> HandlerRegistry registerHandler(String type, Class<T> clazz, MessageHandler<T> handler) {
        handlers.put(type, new HandlerRegistryItem<>(type, clazz, handler));
        return this;
    }

    public void handle(WebSocketSession session, String message) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(message);

        if (!node.isObject()) {
            throw new IllegalArgumentException("Non object messages are not supported");
        }

        handle(session, node);
    }


    private void handle(WebSocketSession session, JsonNode message) throws IOException {
        final JsonNode rawType = message.get("type");
        final JsonNode rawPayload = message.get("payload");

        if (rawType == null || !rawType.isTextual()) {
            throw new IllegalArgumentException("Type must be a String");
        }

        if (rawPayload == null) {
            throw new IllegalArgumentException("Payload must be present");
        }

        final String type = rawType.asText();
        final HandlerRegistryItem<?> item = handlers.get(type);

        if (item == null) {
            throw new IllegalArgumentException(format("Type %s is not registered", type));
        }

        handle(session, rawPayload, item);
    }

    private void handle(WebSocketSession session, JsonNode payload, HandlerRegistryItem handlerRegistryItem) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Object payloadObject = mapper.convertValue(payload, handlerRegistryItem.getClazz());
        handlerRegistryItem.getHandler().handle(session, payloadObject);
    }

}
