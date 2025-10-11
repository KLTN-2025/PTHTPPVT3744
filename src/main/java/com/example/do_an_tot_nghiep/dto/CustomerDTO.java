package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CustomerDTO {
    private Integer customerId;
    private String customerCode;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String customerTier;
    private Integer loyaltyPoints;
    private BigDecimal totalSpent;
    private Integer totalOrders;
    private String status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}
