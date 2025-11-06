package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final MedicalDeviceService deviceService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // ✅ Lấy thông tin nhân viên đăng nhập hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        EmployeeDetails currentEmployee = null;
        if (authentication != null && authentication.getPrincipal() instanceof EmployeeDetails employeeDetails) {
            currentEmployee = employeeDetails;
        }

        LocalDate now = LocalDate.now();
        if (startDate == null) startDate = now.minusDays(30);
        if (endDate == null) endDate = now;

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        DashboardStatisticsDTO stats = dashboardService.getDashboardStatistics(startDateTime, endDateTime);
        Map<String, BigDecimal> revenueData = dashboardService.getRevenueByDateRange(startDateTime, endDateTime);
        Map<String, Long> orderStatusData = dashboardService.getOrderStatusCounts();

        List<OrderResponse> recentOrders = Optional.ofNullable(orderService.getRecentOrders(10))
                .orElse(Collections.emptyList());
        List<CustomerDTO> topCustomers = Optional.ofNullable(customerService.getTopCustomers(10))
                .orElse(Collections.emptyList());
        List<MedicalDeviceDTO> lowStockProducts = Optional.ofNullable(deviceService.getLowStockProducts())
                .orElse(Collections.emptyList());

        Long pendingOrders = dashboardService.getPendingOrdersCount();
        Long unreadNotifications = 0L;

        if (currentEmployee != null) {
            unreadNotifications = notificationService.getUnreadCountByEmployee(currentEmployee.getEmployeeId());
        }

        if (currentEmployee != null) {
            model.addAttribute("currentEmployee", currentEmployee.getEmployee());
        }
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
