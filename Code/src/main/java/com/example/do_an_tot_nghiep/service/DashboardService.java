package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashoardService {
    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;

    @Autowired
    private IMedicalDeviceRepository deviceRepository;

    @Autowired
    private ISupplierRepository supplierRepository;
    @Override
    public DashboardStatisticsDTO getDashboardStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // Get total revenue
        BigDecimal totalRevenue = orderRepository.getTotalRevenue(startDate, endDate);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // Get order counts
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        long totalOrders = orders.size();
        long pendingOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .count();
        long shippingOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.SHIPPING)
                .count();
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .count();

        // Get customer counts
        long totalCustomers = customerRepository.count();
        long newCustomers = customerRepository.countNewCustomers(startDate);

        // Get product counts
        long totalProducts = deviceRepository.count();
        long lowStockProducts = deviceRepository.countLowStockProducts();

        // Calculate today and month revenue
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();
        BigDecimal todayRevenue = orderRepository.getTotalRevenue(todayStart, todayEnd);
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        BigDecimal monthRevenue = orderRepository.getTotalRevenue(monthStart, todayEnd);
        if (monthRevenue == null) {
            monthRevenue = BigDecimal.ZERO;
        }

        return DashboardStatisticsDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .shippingOrders(shippingOrders)
                .completedOrders(completedOrders)
                .totalCustomers(totalCustomers)
                .newCustomers(newCustomers)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .build();
    }
    @Override
    public Map<String, BigDecimal> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);

        // Group by date and sum revenue
        Map<String, BigDecimal> revenueMap = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM")),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        return revenueMap;
    }
    @Override
    public Map<String, Long> getOrderStatusCounts() {

        // 6 trạng thái chuẩn
        Map<String, Long> statusMap = new LinkedHashMap<>();
        statusMap.put("Chờ xác nhận", 0L);
        statusMap.put("Đã xác nhận", 0L);
        statusMap.put("Đang chuẩn bị", 0L);
        statusMap.put("Đang giao", 0L);
        statusMap.put("Hoàn thành", 0L);
        statusMap.put("Đã hủy", 0L);

        // Lấy dữ liệu từ DB
        List<Object[]> results = orderRepository.countOrdersByStatus();

        for (Object[] row : results) {

            // row[0] = ENUM OrderStatus
            Order.OrderStatus statusEnum = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];

            // Convert ENUM → chuỗi tiếng Việt để map vào statusMap
            String vnStatus = convertStatusToVN(statusEnum);

            statusMap.put(vnStatus, count);
        }

        return statusMap;
    }

    private String convertStatusToVN(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case PREPARING -> "Đang chuẩn bị";
            case SHIPPING -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            case RETURNED -> "Trả Hàng";
        };
    }

    @Override
    public BigDecimal getPendingOrdersCount() {
        BigDecimal pendingCount = orderRepository.countByStatus("PENDING");
        return pendingCount != null ? pendingCount : BigDecimal.ZERO;
    }
}
