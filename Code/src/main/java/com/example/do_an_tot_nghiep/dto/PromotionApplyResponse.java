package com.example.do_an_tot_nghiep.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApplyResponse {
    private Boolean success;
    private String message;
    private BigDecimal discountAmount;
    private Integer promotionId;
}
