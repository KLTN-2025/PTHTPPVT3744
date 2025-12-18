package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.OrderDetailDTO;
import com.example.do_an_tot_nghiep.dto.OrderResponse;
import com.example.do_an_tot_nghiep.dto.OrderStatsDTO;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderService orderService;

    // ==========================
    //  📌 Danh sách + Lọc + Trang
    // ==========================
    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String payment,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        OrderStatsDTO stats = orderService.getStats();
        Page<Order> orderPage = orderService.searchOrders(
                keyword,
                status,
                payment,
                from,
                to,
                PageRequest.of(page, 10)
        );
        model.addAttribute("stats", stats);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements()); // ⬅️ Tổng số đơn

        // Thống kê theo trạng thái
        model.addAttribute("statusCounts", orderService.getStatusCounts());

        // Giữ lại giá trị filter để trả về view
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", payment);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        return "order/orders-list";
    }


    // ==========================
    //   📌 Xem chi tiết đơn hàng
    // ==========================
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Integer id, Model model) {
        OrderResponse order = orderService.getOrderById(id);
        List<OrderDetailDTO> items = orderService.getOrderItems(id);
        model.addAttribute("order", order);
        model.addAttribute("orderItems", items);
        return "order/order-detail"; // trang chi tiết bạn tạo sau
    }

    // ==========================
    //   📌 Sửa trạng thái đơn hàng
    // ==========================
    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable Integer id, Model model) {
        OrderResponse order = orderService.getOrderById(id);

        model.addAttribute("order", order);
        model.addAttribute("statuses", Order.OrderStatus.values());

        return "admin/orders/order-edit";
    }

    @PostMapping("/edit")
    public String updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam Order.OrderStatus status
    ) {
        orderService.updateStatus(orderId, status);
        return "redirect:/admin/orders?success";
    }

    // ==========================
    //   📌 Xóa đơn hàng
    // ==========================
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteOrder(@PathVariable Integer id) {
        Map<String, Object> res = new HashMap<>();
        try {
            orderService.deleteOrder(id);
            res.put("success", true);
            res.put("message", "Xóa đơn hàng thành công!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }
    @PostMapping("/delete-batch")
    @ResponseBody
    public Map<String, Object> deleteBatch(@RequestBody List<Integer> ids) {
        Map<String, Object> res = new HashMap<>();
        try {
            orderService.deleteBatch(ids);
            res.put("success", true);
            res.put("message", "Đã xóa " + ids.size() + " đơn hàng.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }
    @PostMapping("/update-status")
    public String updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam Order.OrderStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateStatus(orderId, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/orders/view/" + orderId;
    }
    @GetMapping("/invoice/{id}")
    public String printInvoice(@PathVariable("id") Integer id, Model model) {
        OrderResponse order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        return "invoice/invoice";
    }
}
