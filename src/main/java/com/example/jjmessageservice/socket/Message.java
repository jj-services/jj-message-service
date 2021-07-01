package com.example.jjmessageservice.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class Message<T> {
    private final String type;
    private final T payload;

    public Message(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public String toJson() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
