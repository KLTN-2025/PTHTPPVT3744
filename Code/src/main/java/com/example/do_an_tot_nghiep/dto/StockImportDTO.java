package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockImportDTO {
    private Integer importId;
    private String importCode;
    private Integer supplierId;
    private String supplierName;
    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String note;
    private String status;
    private Integer createdById;
    private String createdByName;
    private Integer approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<StockImportDetailDTO> importDetails = new ArrayList<>();
}
