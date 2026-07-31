package com.ecommerce.service;

import com.ecommerce.repository.ProductRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ProductRepository productRepository;

    public ChatService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String chat(String userMessage) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + apiKey;

        // Give the AI live context about what's actually in stock
        String productContext = productRepository.findAll().stream()
                .limit(30)
                .map(p -> "- " + p.getName() + " (₹" + p.getPrice() + ", "
                        + (p.getStock() > 0 ? "in stock" : "out of stock") + ", category: " + p.getCategory() + ")")
                .collect(Collectors.joining("\n"));

        String systemPrompt = "You are a friendly shopping assistant for an online store called ShopEasy. "
                + "Answer questions about products, help with sizing/comparisons, and give shopping advice. "
                + "Keep answers short and conversational (2-4 sentences max). "
                + "Here is the current product catalog:\n" + productContext
                + "\n\nIf asked about something not in this list, say it's not currently available. "
                + "Do not make up products or prices.";

        JSONObject systemInstruction = new JSONObject()
                .put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt)));

        JSONObject userPart = new JSONObject().put("text", userMessage);
        JSONObject userContent = new JSONObject()
                .put("role", "user")
                .put("parts", new JSONArray().put(userPart));

        JSONObject body = new JSONObject()
                .put("system_instruction", systemInstruction)
                .put("contents", new JSONArray().put(userContent));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            JSONObject json = new JSONObject(response);
            return json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I'm having trouble responding right now. Please try again in a moment.";
        }
    }
}