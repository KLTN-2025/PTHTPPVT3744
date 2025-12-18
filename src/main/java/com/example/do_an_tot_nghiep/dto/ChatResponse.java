package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@lombok.Builder
public class ChatResponse {
    private String conversationId;
    private String message;
    private Long timestamp;
}