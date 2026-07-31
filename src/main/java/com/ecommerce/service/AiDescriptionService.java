package com.ecommerce.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiDescriptionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateDescription(String productName, String category) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + apiKey;

        String prompt = "Write a concise, appealing e-commerce product description (2-3 sentences, "
                + "no headings, no markdown) for a product called \"" + productName + "\""
                + (category != null && !category.isBlank() ? " in the category \"" + category + "\"" : "")
                + ". Focus on what makes it appealing to a buyer.";

        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

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
            return "Could not generate a description right now — please write one manually.";
        }
    }
}