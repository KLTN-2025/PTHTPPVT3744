package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private String orderCode;
    private String customerName;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal loyaltyDiscount;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderDetailDTO> items;
}
