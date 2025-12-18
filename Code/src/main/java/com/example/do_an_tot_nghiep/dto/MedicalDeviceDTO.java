package com.example.do_an_tot_nghiep.dto;

import com.example.do_an_tot_nghiep.model.MedicalDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDeviceDTO {

    private String deviceId;
    private String name;
    private String slug;
    private String sku;

    private Integer categoryId;
    private Integer brandId;
    private Integer supplierId;

    private String categoryName;
    private String brandName;
    private String supplierName;

    private String description;
    private String specification;
    private String usageInstruction;

    private Double price;
    private Double originalPrice;
    private Integer discountPercent;

    private Integer stockQuantity;
    private Integer minStockLevel;

    private String unit;
    private Double weight;
    private String dimensions;
    private Integer warrantyPeriod;

    private MedicalDevice.DeviceStatus status;
    private Boolean isFeatured;
    private Boolean isNew;

    private Integer viewCount;
    private Integer soldCount;

    private String imageUrl;
    private MultipartFile imageFile;           // upload ảnh chính từ frontend

    private List<String> galleryUrls;          // lưu dưới dạng CSV trong DB
    private List<MultipartFile> galleryFiles;  // upload gallery từ frontend

    private String metaKeywords;
    private String metaDescription;

    private Double avgRating;
    private Long reviewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String imagePublicId;
}
