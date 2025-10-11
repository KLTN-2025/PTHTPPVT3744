package com.example.do_an_tot_nghiep.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItemRequest {
    private String deviceId;
    private Integer quantity;
}
