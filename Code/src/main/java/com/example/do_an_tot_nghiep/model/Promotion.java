package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer promotionId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /* ================= DISCOUNT ================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType = DiscountType.PERCENT;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    /* ================= USAGE ================= */

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "usage_per_customer", nullable = false)
    private Integer usagePerCustomer = 1;

    /* ================= CUSTOMER TIER ================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier", nullable = false)
    private CustomerTier customerTier = CustomerTier.ALL;

    /* ================= APPLY ================= */

    @Enumerated(EnumType.STRING)
    @Column(name = "applicable_to", nullable = false)
    private ApplicableTo applicableTo = ApplicableTo.ALL;

    /* ================= TIME ================= */

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    /* ================= STATUS ================= */

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /* ================= AUDIT ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ================= ENUMS ================= */

    public enum DiscountType {
        PERCENT,
        FIXED,
        FREESHIP
    }

    public enum ApplicableTo {
        ALL,
        CATEGORY,
        PRODUCT
    }

    public enum CustomerTier {
        ALL,
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM
    }
}
