package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Integer customerId;
    private String customerCode;
    private String username;
    private String passwordHash;

    // ✅ OAuth2 fields
    private String provider;        // LOCAL, GOOGLE, FACEBOOK
    private String providerId;      // OAuth2 Provider ID
    private Boolean hasCustomPassword;

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
    private LocalDateTime lastLogin;
    private LocalDateTime lastOrderDate;
    private String referralCode;
    private Integer referredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}