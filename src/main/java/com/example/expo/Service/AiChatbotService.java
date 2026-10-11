package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.AiChatbot;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.AiChatbotRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiChatbotService {
    private final AiChatbotRepository aiChatbotRepository;
    private final VisitorRepository visitorRepository;

    public List<AiChatbot> getAllAiChatbots(){
        return aiChatbotRepository.findAll();
    }

    public void addAiChatbot(Integer visitorId, AiChatbot aiChatbot){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        if(visitor.getAiChatbot() != null){
            throw new ApiException("this visitor already has aiChatbot");
        }
        aiChatbot.setVisitor(visitor);
        aiChatbotRepository.save(aiChatbot);
    }

    public void deleteAiChatbot(Integer id){
        AiChatbot aiChatbot = aiChatbotRepository.findAiChatbotById(id);
        if(aiChatbot == null){
            throw new ApiException("aiChatbot not found");
        }
        aiChatbot.getVisitor().setAiChatbot(null);
        aiChatbotRepository.delete(aiChatbot);
    }
}
