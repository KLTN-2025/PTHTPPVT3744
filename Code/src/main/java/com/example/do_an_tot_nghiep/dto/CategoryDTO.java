package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private Integer parentId;
    private String parentName;
    private String slug;
    private String description;
    private String imageUrl;
    private String metaTitle;
    private String metaDescription;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}