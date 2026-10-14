package com.example.expo.Controller;

import com.example.expo.Dto.In.ChatQuestionRequest;
import com.example.expo.Service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @GetMapping("/suggestions/{visitorId}")
    public ResponseEntity<?> getSuggestions(@PathVariable Integer visitorId){
        return ResponseEntity.status(200).body(chatbotService.getSuggestions(visitorId));
    }

    @PostMapping("/ask/{visitorId}")
    public ResponseEntity<?> ask(@PathVariable Integer visitorId, @RequestBody @Valid ChatQuestionRequest request){
        return ResponseEntity.status(200).body(chatbotService.ask(visitorId, request));
    }
}
