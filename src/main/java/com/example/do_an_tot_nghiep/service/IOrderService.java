package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.OrderRequest;
import com.example.do_an_tot_nghiep.dto.OrderResponse;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.model.OrderDetail;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface IOrderService {
    @Transactional
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Integer orderId);

    @Transactional
    void updateOrderStatus(Integer orderId, String newStatus, Integer employeeId);

    void createOrderStatusHistory(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus, Employee employee);

    void restoreStock(Order order);

    BigDecimal calculateShippingFee(BigDecimal subtotal);

    //tạo mã đơn hàng
    String generateOrderCode();

    OrderResponse convertToOrderResponse(Order order, List<OrderDetail> details);

    List<OrderResponse> getRecentOrders(int i);
}
