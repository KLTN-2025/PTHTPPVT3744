package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChatRequest {
    private String message;
    private String conversationId;
    private String userId; // Optional: ID khách hàng nếu đã đăng nhập
}
