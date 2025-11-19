package com.example.do_an_tot_nghiep.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_device")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalDevice {
    @Id
    @Column(name = "device_id", length = 50)
    private String deviceId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "specification", columnDefinition = "TEXT")
    private String specification;

    @Column(name = "usage_instruction", columnDefinition = "TEXT")
    private String usageInstruction;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;

    @Column(name = "unit", length = 50)
    private String unit = "Cái";

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "warranty_period")
    private Integer warrantyPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeviceStatus status = DeviceStatus.Còn_hàng;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_new")
    private Boolean isNew = false;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "gallery_urls", columnDefinition = "TEXT")
    private String galleryUrls;

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    private String metaKeywords;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DeviceStatus {
        Còn_hàng("Còn hàng"),
        Hết_hàng("Hết hàng"),
        Ngừng_bán("Ngừng bán");

        private final String displayName;

        DeviceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @Transient
    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .filter(r -> r.getStatus() == Review.ReviewStatus.APPROVED && r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Transient
    public Long getTotalReviews() {
        if (reviews == null) return 0L;
        return reviews.stream()
                .filter(r -> r.getStatus() == Review.ReviewStatus.APPROVED)
                .count();
    }
    public List<String> getGalleryUrlList() {
        if (galleryUrls == null || galleryUrls.isEmpty()) return new ArrayList<>();
        return List.of(galleryUrls.split(","));
    }

    public void setGalleryUrlList(List<String> urls) {
        this.galleryUrls = String.join(",", urls);
    }
}
