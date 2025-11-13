package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDeviceDTO {
    private String deviceId;
    private Integer categoryId;
    private Integer brandId;
    private String name;
    private String sku;
    private String categoryName;
    private String brandName;
    private String supplierName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercent;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String status;
    private String imageUrl;
    private Integer viewCount;
    private Integer soldCount;
    private Double avgRating;
    private Long reviewCount;
    private Boolean isFeatured;
    private Boolean isNew;
    private String description;
}
