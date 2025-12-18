package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.service.ChatService;
import com.example.do_an_tot_nghiep.service.ConversationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Hoặc chỉ định cụ thể origins
public class ChatController {

    private final ChatService chatService;
    private final ConversationHistoryService historyService;

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatRequest request) {
        try {
            // Lấy thông tin user đang đăng nhập
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            log.info("Chat request from user: {}", username);

            // Validate input
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tin nhắn không được để trống"));
            }

            if (request.getMessage().length() > 1000) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tin nhắn quá dài (tối đa 1000 ký tự)"));
            }

            // Tạo conversation ID dựa trên username nếu chưa có
            // Mỗi user sẽ có conversation riêng
            String conversationId = request.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = "customer_" + username + "_" + System.currentTimeMillis();
            }

            // Lưu thông tin customer vào metadata (lần đầu)
            if (historyService.getContext(conversationId) == null) {
                historyService.setMetadata(conversationId, "username", username);
                historyService.setMetadata(conversationId, "role", "CUSTOMER");
            }

            // Xử lý tin nhắn
            String response = chatService.chat(request.getMessage(), conversationId);

            return ResponseEntity.ok(ChatResponse.builder()
                    .conversationId(conversationId)
                    .message(response)
                    .timestamp(System.currentTimeMillis())
                    .build());

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "Lỗi hệ thống",
                            "message", "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau."
                    ));
        }
    }

    @GetMapping("/history/{conversationId}")
    public ResponseEntity<?> getHistory(@PathVariable String conversationId) {
        try {
            // Kiểm tra quyền: user chỉ được xem history của mình
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            ConversationHistoryService.ConversationContext context =
                    historyService.getContext(conversationId);

            if (context == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify ownership
            String conversationOwner = (String) context.getMetadata().get("username");
            if (conversationOwner != null && !conversationOwner.equals(username)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Bạn không có quyền xem lịch sử này"));
            }

            return ResponseEntity.ok(ConversationHistoryResponse.builder()
                    .conversationId(conversationId)
                    .messages(context.getMessages())
                    .metadata(context.getMetadata())
                    .build());
        } catch (Exception e) {
            log.error("Error getting conversation history", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi khi tải lịch sử"));
        }
    }

    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<?> clearHistory(@PathVariable String conversationId) {
        try {
            // Kiểm tra quyền: user chỉ được xóa history của mình
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            ConversationHistoryService.ConversationContext context =
                    historyService.getContext(conversationId);

            if (context != null) {
                String conversationOwner = (String) context.getMetadata().get("username");
                if (conversationOwner != null && !conversationOwner.equals(username)) {
                    return ResponseEntity.status(403)
                            .body(Map.of("error", "Bạn không có quyền xóa lịch sử này"));
                }
            }

            historyService.clearHistory(conversationId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa lịch sử thành công"));
        } catch (Exception e) {
            log.error("Error clearing conversation history", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi khi xóa lịch sử"));
        }
    }

    @PostMapping("/metadata/{conversationId}")
    public ResponseEntity<?> setMetadata(
            @PathVariable String conversationId,
            @RequestBody MetadataRequest request) {
        try {
            // Kiểm tra quyền
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            ConversationHistoryService.ConversationContext context =
                    historyService.getContext(conversationId);

            if (context != null) {
                String conversationOwner = (String) context.getMetadata().get("username");
                if (conversationOwner != null && !conversationOwner.equals(username)) {
                    return ResponseEntity.status(403)
                            .body(Map.of("error", "Bạn không có quyền sửa metadata này"));
                }
            }

            historyService.setMetadata(conversationId, request.getKey(), request.getValue());
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật metadata"));
        } catch (Exception e) {
            log.error("Error setting metadata", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi khi cập nhật metadata"));
        }
    }

    // Lấy danh sách conversations của user hiện tại
    @GetMapping("/my-conversations")
    public ResponseEntity<?> getMyConversations() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Lấy tất cả conversations của user này
            var allConversations = historyService.getAllConversations();
            var myConversations = allConversations.stream()
                    .filter(conv -> {
                        String owner = (String) conv.getMetadata().get("username");
                        return username.equals(owner);
                    })
                    .map(conv -> Map.of(
                            "conversationId", conv.getConversationId(),
                            "createdAt", conv.getCreatedAt().toString(),
                            "lastActiveAt", conv.getLastActiveAt().toString(),
                            "messageCount", conv.getMessages().size(),
                            "metadata", conv.getMetadata()
                    ))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "conversations", myConversations,
                    "total", myConversations.size()
            ));
        } catch (Exception e) {
            log.error("Error getting conversations", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Lỗi khi tải danh sách hội thoại"));
        }
    }

    // ===== TEST ENDPOINTS =====

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        // Lấy thông tin user nếu đã login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "Chat Service",
                "user", username,
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        log.info("Test endpoint called by user: {}", username);

        return ResponseEntity.ok(Map.of(
                "message", "Chat API is working!",
                "user", username,
                "role", auth.getAuthorities().toString(),
                "endpoints", Map.of(
                        "sendMessage", "POST /api/chat/message",
                        "getHistory", "GET /api/chat/history/{conversationId}",
                        "clearHistory", "DELETE /api/chat/history/{conversationId}",
                        "health", "GET /api/chat/health"
                )
        ));
    }
}