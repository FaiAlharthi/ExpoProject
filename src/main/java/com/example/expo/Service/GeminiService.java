package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:}")
    private String apiUrl;

    @Value("${ai.model:}")
    private String model;

    private final RestClient restClient;

    public GeminiService(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    // sends the prompt and returns the answer text (json)
    public String ask(String prompt){
        if(apiKey == null || apiKey.isBlank()){
            throw new ApiException("AI key is missing");
        }
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );
        Map<?, ?> response = restClient.post()
                .uri(apiUrl + "/" + model + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
        return readText(response);
    }

    // answer is in candidates[0].content.parts[].text
    private String readText(Map<?, ?> response){
        if(response == null || !(response.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()){
            throw new ApiException("AI returned no answer");
        }
        Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
        if(content == null || !(content.get("parts") instanceof List<?> parts)){
            throw new ApiException("AI returned no answer");
        }
        StringBuilder text = new StringBuilder();
        for(Object part : parts){
            Map<?, ?> p = (Map<?, ?>) part;
            if(p.get("text") != null && !Boolean.TRUE.equals(p.get("thought"))){
                text.append(p.get("text"));
            }
        }
        if(text.isEmpty()){
            throw new ApiException("AI returned no answer");
        }
        return text.toString();
    }
}
