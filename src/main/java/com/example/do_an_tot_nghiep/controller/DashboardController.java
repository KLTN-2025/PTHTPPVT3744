package com.example.do_an_tot_nghiep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private MedicalDeviceService deviceService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal EmployeeDetails currentEmployee,
            Model model) {

        // Set default date range if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Convert to LocalDateTime for queries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get dashboard statistics
        DashboardStatisticsDTO stats = dashboardService.getDashboardStatistics(startDateTime, endDateTime);

        // Get revenue data for chart
        Map<String, BigDecimal> revenueData = dashboardService.getRevenueByDateRange(startDateTime, endDateTime);

        // Get order status data for pie chart
        Map<String, Long> orderStatusData = dashboardService.getOrderStatusCounts();

        // Get recent orders
        List<OrderResponse> recentOrders = orderService.getRecentOrders(10);

        // Get top customers (VIP)
        List<CustomerDTO> topCustomers = customerService.getTopCustomers(10);

        // Get low stock products
        List<MedicalDeviceDTO> lowStockProducts = deviceService.getLowStockProducts();

        // Get pending orders count for sidebar badge
        Long pendingOrders = dashboardService.getPendingOrdersCount();

        // Get unread notifications count
        Long unreadNotifications = notificationService.getUnreadCountByEmployee(currentEmployee.getEmployeeId());

        // Add data to model
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("stats", stats);
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("orderStatusData", orderStatusData);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("topCustomers", topCustomers);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("lowStockCount", lowStockProducts.size());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/dashboard";
    }

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

}
