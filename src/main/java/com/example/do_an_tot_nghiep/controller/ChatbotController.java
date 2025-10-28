package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.ChatRequest;
import com.example.do_an_tot_nghiep.dto.ChatResponse;
import com.example.do_an_tot_nghiep.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            String response = chatbotService.getResponse(request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response, true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ChatResponse(
                    "Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.", false
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot is running");
    }
}