package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private Integer customerId;
    private Integer addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String paymentMethod;
    private String promotionCode;
    private Integer loyaltyPointsUsed;
    private String note;
    private List<OrderItemRequest> items;
}
