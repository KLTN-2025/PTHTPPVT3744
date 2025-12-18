package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDTO {
    private Integer brandId;
    private String name;
    private String slug;
    private String country;
    private String logoUrl;
    private String description;
    private String website;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Thống kê
    private Long productCount; // Số lượng sản phẩm của brand
}