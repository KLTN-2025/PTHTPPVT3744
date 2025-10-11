package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApplyRequest {
    private String promotionCode;
    private Integer customerId;
    private BigDecimal orderAmount;
}
