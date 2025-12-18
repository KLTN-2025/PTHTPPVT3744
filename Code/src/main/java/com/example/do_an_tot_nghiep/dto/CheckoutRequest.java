package com.example.do_an_tot_nghiep.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {
    private Integer addressId;           // ID địa chỉ có sẵn (nếu chọn)
    private String receiverName;         // Tên người nhận
    private String receiverPhone;        // SĐT người nhận
    private String receiverAddress;      // Địa chỉ đầy đủ
    private String paymentMethod;        // COD, VNPAY, MOMO
    private String promotionCode;        // Mã giảm giá
    private Integer loyaltyPointsUsed;   // Số điểm tích lũy sử dụng
    private String note;                 // Ghi chú từ khách hàng
}