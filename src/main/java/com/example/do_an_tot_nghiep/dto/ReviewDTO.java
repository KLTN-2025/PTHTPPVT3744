package com.example.do_an_tot_nghiep.dto;

import com.example.do_an_tot_nghiep.model.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Integer reviewId;

    // Customer info
    private Integer customerId;
    private String customerName;
    private String customerAvatar;
    private String customerEmail;

    // Product info
    private String deviceId;  // Đổi từ Integer sang String
    private String deviceName;
    private String deviceImage;

    // Order info
    private Integer orderId;
    private String orderCode;

    // Review content
    private Integer rating;
    private String comment;
    private String images; // JSON string or comma-separated
    private List<String> imageList; // Parsed images

    private Boolean isVerifiedPurchase;

    // Admin reply
    private String adminReply;
    private Integer repliedById;
    private String repliedByName;
    private LocalDateTime repliedAt;

    // Status
    private Review.ReviewStatus status;
    private String statusDisplay;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}