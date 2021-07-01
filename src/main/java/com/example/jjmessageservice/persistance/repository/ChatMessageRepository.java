package com.example.jjmessageservice.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageRepository, Long> {
}
