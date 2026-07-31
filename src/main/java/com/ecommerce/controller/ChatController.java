package com.ecommerce.controller;

import com.ecommerce.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String reply = chatService.chat(body.get("message"));
        return Map.of("reply", reply);
    }
}