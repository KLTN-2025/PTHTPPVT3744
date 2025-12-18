package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.DashboardStatisticsDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface IDashoardService {
    DashboardStatisticsDTO getDashboardStatistics(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, BigDecimal> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getOrderStatusCounts();

    BigDecimal getPendingOrdersCount();
}
