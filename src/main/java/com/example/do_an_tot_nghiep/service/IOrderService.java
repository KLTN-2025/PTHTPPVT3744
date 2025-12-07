package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.OrderRequest;
import com.example.do_an_tot_nghiep.dto.OrderResponse;
import com.example.do_an_tot_nghiep.dto.OrderStatsDTO;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.model.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    Page<Order> searchOrders(String keyword, String status, String paymentMethod, String fromDate, String toDate, PageRequest of);

    void updateStatus(Integer orderId, Order.OrderStatus status);

    void deleteOrder(Integer id);

    Map<String, Long> getStatusCounts();

    OrderStatsDTO getStats();

    void deleteBatch(List<Integer> ids);
}
