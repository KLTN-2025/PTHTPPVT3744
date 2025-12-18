package com.example.do_an_tot_nghiep.dto;

import com.example.do_an_tot_nghiep.service.ConversationHistoryService;
import lombok.Data;

@Data
@lombok.Builder
public class ConversationHistoryResponse {
    private String conversationId;
    private java.util.List<ConversationHistoryService.Message> messages;
    private java.util.Map<String, Object> metadata;
}