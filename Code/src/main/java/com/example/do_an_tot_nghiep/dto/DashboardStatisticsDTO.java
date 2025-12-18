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
public class DashboardStatisticsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long shippingOrders;
    private Long completedOrders;
    private Long totalCustomers;
    private Long newCustomers;
    private Long totalProducts;
    private Long lowStockProducts;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
}
