package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.repository.IOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderSuccessController {

    @Autowired
    private IOrderRepository orderRepository;

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam(required = false) String orderCode, Model model) {

        if (orderCode == null || orderCode.isEmpty()) {
            return "redirect:/";
        }

        // Tìm đơn hàng theo orderCode
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);

        if (order != null) {
            model.addAttribute("orderCode", order.getOrderCode());
            model.addAttribute("orderDate", order.getCreatedAt());
            model.addAttribute("totalAmount", order.getTotalPrice());
            model.addAttribute("paymentMethod", getPaymentMethodText(order.getPaymentMethod()));
            model.addAttribute("receiverName", order.getReceiverName());
            model.addAttribute("receiverPhone", order.getReceiverPhone());
            model.addAttribute("receiverAddress", order.getReceiverAddress());
        } else {
            model.addAttribute("orderCode", orderCode);
        }

        model.addAttribute("title", "Đặt hàng thành công - Vật Tư Y Tế ABC");
        return "order/order-success";
    }

    private String getPaymentMethodText(Order.PaymentMethod paymentMethod) {
        if (paymentMethod == null) return "Chưa xác định";

        switch (paymentMethod) {
            case COD: return "Thanh toán khi nhận hàng (COD)";
            case VNPAY: return "VNPay";
            case MOMO: return "MoMo";
            default: return paymentMethod.toString();
        }
    }
}