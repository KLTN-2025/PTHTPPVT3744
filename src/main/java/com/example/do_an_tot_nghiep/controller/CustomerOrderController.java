package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.OrderResponse;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import com.example.do_an_tot_nghiep.repository.IOrderRepository;
import com.example.do_an_tot_nghiep.security.CustomerUserDetails;
import com.example.do_an_tot_nghiep.security.CustomOAuth2User;
import com.example.do_an_tot_nghiep.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CustomerOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    // ==================== HIỂN THỊ TRANG ĐƠN HÀNG ====================
    @GetMapping("/my-orders")
    public String showMyOrders(Model model) {
        Customer customer = getCurrentCustomer();

        if (customer == null) {
            return "redirect:/login?returnUrl=/my-orders";
        }

        model.addAttribute("customer", customer);
        model.addAttribute("title", "Đơn hàng của tôi - Vật Tư Y Tế ABC");
        return "customer/my-orders";
    }

    // ==================== API: LẤY DANH SÁCH ĐƠN HÀNG ====================
    @GetMapping("/api/customer/orders")
    @ResponseBody
    public ResponseEntity<?> getCustomerOrders() {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // ✅ Sử dụng method có sẵn trong OrderService
            List<OrderResponse> orders = orderService.getCustomerOrders(customer.getCustomerId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orders", orders
            ));

        } catch (Exception e) {
            System.err.println("Error getting orders: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // ==================== API: HỦY ĐƠN HÀNG ====================
    @PostMapping("/api/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // ✅ Kiểm tra đơn hàng có thuộc về customer này không
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if (!order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền hủy đơn hàng này"
                ));
            }

            // ✅ Chỉ cho phép hủy đơn ở trạng thái PENDING
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Chỉ có thể hủy đơn hàng đang chờ xác nhận"
                ));
            }

            // ✅ Sử dụng method có sẵn trong OrderService
            orderService.updateStatus(orderId, Order.OrderStatus.CANCELLED);

            // ✅ Hoàn lại kho
            orderService.restoreStock(order);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã hủy đơn hàng thành công"
            ));

        } catch (Exception e) {
            System.err.println("Error canceling order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // ==================== API: MUA LẠI (REORDER) ====================
    @PostMapping("/api/orders/{orderId}/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderOrder(@PathVariable Integer orderId) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // ✅ Kiểm tra đơn hàng có thuộc về customer này không
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if (!order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền thực hiện thao tác này"
                ));
            }

            // TODO: Implement logic thêm các sản phẩm trong đơn hàng vào giỏ hàng
            // Bạn có thể tạo CartService.addOrderToCart(order, customer)

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm sản phẩm vào giỏ hàng"
            ));

        } catch (Exception e) {
            System.err.println("Error reordering: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // ==================== HIỂN THỊ CHI TIẾT ĐƠN HÀNG ====================
    @GetMapping("/order-detail/{orderId}")
    public String showOrderDetail(@PathVariable Integer orderId, Model model) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return "redirect:/login?returnUrl=/order-detail/" + orderId;
            }

            // ✅ Kiểm tra quyền truy cập
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if (!order.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                model.addAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này");
                return "error/403";
            }

            // ✅ Lấy thông tin đơn hàng
            OrderResponse orderResponse = orderService.getOrderById(orderId);

            model.addAttribute("order", orderResponse);
            model.addAttribute("customer", customer);
            model.addAttribute("title", "Chi tiết đơn hàng #" + order.getOrderCode());

            return "customer/order-detail";

        } catch (Exception e) {
            System.err.println("Error loading order detail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin đơn hàng");
            return "error/500";
        }
    }

    // ==================== HELPER METHOD ====================
    private Customer getCurrentCustomer() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            // Customer login bằng username/password
            if (principal instanceof CustomerUserDetails) {
                return ((CustomerUserDetails) principal).getCustomer();
            }

            // Customer login bằng OAuth2 (Google, Facebook)
            if (principal instanceof CustomOAuth2User) {
                String email = ((CustomOAuth2User) principal).getEmail();
                return customerRepository.findByEmail(email).orElse(null);
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error getting current customer: " + e.getMessage());
            return null;
        }
    }
}