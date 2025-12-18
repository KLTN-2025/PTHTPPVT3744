package com.example.do_an_tot_nghiep.service;

import org.springframework.stereotype.Service;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationHistoryService {

    private final Map<String, ConversationContext> conversations = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_SIZE = 10; // Lưu tối đa 10 tin nhắn gần nhất

    @Data
    public static class Message {
        private String role; // "user" hoặc "assistant"
        private String content;
        private LocalDateTime timestamp;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }
    }

    @Data
    public static class ConversationContext {
        private String conversationId;
        private List<Message> messages;
        private LocalDateTime createdAt;
        private LocalDateTime lastActiveAt;
        private Map<String, Object> metadata; // Lưu thông tin thêm (tên khách, preferences...)

        public ConversationContext(String conversationId) {
            this.conversationId = conversationId;
            this.messages = new ArrayList<>();
            this.createdAt = LocalDateTime.now();
            this.lastActiveAt = LocalDateTime.now();
            this.metadata = new HashMap<>();
        }

        public void addMessage(Message message) {
            this.messages.add(message);
            this.lastActiveAt = LocalDateTime.now();

            // Giới hạn số lượng tin nhắn
            if (messages.size() > MAX_HISTORY_SIZE * 2) { // *2 vì mỗi lượt có 2 tin (user + bot)
                messages.remove(0); // Xóa tin nhắn cũ nhất
            }
        }
    }

    public void addMessage(String conversationId, String userMessage, String assistantResponse) {
        ConversationContext context = conversations.computeIfAbsent(
                conversationId,
                id -> new ConversationContext(id)
        );

        context.addMessage(new Message("user", userMessage));
        context.addMessage(new Message("assistant", assistantResponse));
    }

    public String getHistory(String conversationId) {
        ConversationContext context = conversations.get(conversationId);

        if (context == null || context.getMessages().isEmpty()) {
            return "(Chưa có lịch sử hội thoại)";
        }

        StringBuilder history = new StringBuilder();
        for (Message msg : context.getMessages()) {
            String role = msg.getRole().equals("user") ? "Khách" : "Em";
            history.append(String.format("[%s]: %s\n", role, msg.getContent()));
        }

        return history.toString();
    }

    public ConversationContext getContext(String conversationId) {
        return conversations.get(conversationId);
    }

    public void clearHistory(String conversationId) {
        conversations.remove(conversationId);
    }

    public void setMetadata(String conversationId, String key, Object value) {
        ConversationContext context = conversations.computeIfAbsent(
                conversationId,
                id -> new ConversationContext(id)
        );
        context.getMetadata().put(key, value);
    }

    public Object getMetadata(String conversationId, String key) {
        ConversationContext context = conversations.get(conversationId);
        return context != null ? context.getMetadata().get(key) : null;
    }

    // Dọn dẹp các conversation cũ (gọi định kỳ)
    public void cleanupOldConversations(int hoursInactive) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hoursInactive);
        conversations.entrySet().removeIf(entry ->
                entry.getValue().getLastActiveAt().isBefore(cutoff)
        );
    }

    // Lấy tất cả conversations
    public java.util.Collection<ConversationContext> getAllConversations() {
        return conversations.values();
    }
}