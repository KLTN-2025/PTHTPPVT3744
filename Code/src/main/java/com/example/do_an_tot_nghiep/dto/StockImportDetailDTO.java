package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockImportDetailDTO {
    private Integer importDetailId;
    private Integer importId;
    private Integer deviceId;
    private String deviceName;
    private String deviceImage;
    private Integer quantity;
    private BigDecimal importPrice;
    private BigDecimal totalPrice;
    private LocalDate expiryDate;
    private String batchNumber;
}