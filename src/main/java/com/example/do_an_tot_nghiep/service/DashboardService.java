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
        Map<String, Long> statusMap = new LinkedHashMap<>();

        statusMap.put("Chờ xác nhận", orderRepository.countByStatus(Order.OrderStatus.PENDING));
        statusMap.put("Đã xác nhận", orderRepository.countByStatus(Order.OrderStatus.CONFIRMED));
        statusMap.put("Đang chuẩn bị", orderRepository.countByStatus(Order.OrderStatus.PREPARING));
        statusMap.put("Đang giao", orderRepository.countByStatus(Order.OrderStatus.SHIPPING));
        statusMap.put("Hoàn thành", orderRepository.countByStatus(Order.OrderStatus.COMPLETED));
        statusMap.put("Đã hủy", orderRepository.countByStatus(Order.OrderStatus.CANCELLED));

        return statusMap;
    }
    @Override
    public Long getPendingOrdersCount() {
        return orderRepository.countByStatus(Order.OrderStatus.PENDING);
    }
}
