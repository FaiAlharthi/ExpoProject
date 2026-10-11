package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.AiChatbot;
import com.example.expo.Service.AiChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-chatbot")
@RequiredArgsConstructor
public class AiChatbotController {
    private final AiChatbotService aiChatbotService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllAiChatbots(){
        return ResponseEntity.status(200).body(aiChatbotService.getAllAiChatbots());
    }

    @PostMapping("/createAiChatbot/{visitorId}")
    public ResponseEntity<?> createAiChatbot(@PathVariable Integer visitorId, @RequestBody @Valid AiChatbot aiChatbot){
        aiChatbotService.addAiChatbot(visitorId, aiChatbot);
        return ResponseEntity.status(200).body(new ApiResponse("aiChatbot added"));
    }

    @DeleteMapping("/deleteAiChatbot/{id}")
    public ResponseEntity<?> deleteAiChatbot(@PathVariable Integer id){
        aiChatbotService.deleteAiChatbot(id);
        return ResponseEntity.status(200).body(new ApiResponse("aiChatbot deleted"));
    }
}
