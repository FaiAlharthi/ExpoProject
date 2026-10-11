package com.example.expo.Repository;

import com.example.expo.Model.AiChatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiChatbotRepository extends JpaRepository<AiChatbot, Integer> {
    AiChatbot findAiChatbotById(Integer id);
}
