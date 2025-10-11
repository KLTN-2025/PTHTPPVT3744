package com.example.do_an_tot_nghiep.service;


import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {
    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private IMedicalDeviceRepository deviceRepository;

    @Autowired
    private IPromotionRepository promotionRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private IOrderDetailRepository orderDetailRepository;

    @Autowired
    private IEmployeeRepository employeeRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private IOrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional
    @Override
    public OrderResponse createOrder(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Calculate order totals
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {
            MedicalDevice device = deviceRepository.findById(item.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found: " + item.getDeviceId()));

            // Check stock
            if (device.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + device.getName());
            }

            BigDecimal itemTotal = device.getPrice().multiply(new BigDecimal(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            OrderDetail detail = OrderDetail.builder()
                    .device(device)
                    .deviceName(device.getName())
                    .deviceImage(device.getImageUrl())
                    .quantity(item.getQuantity())
                    .unitPrice(device.getPrice())
                    .totalPrice(itemTotal)
                    .build();

            orderDetails.add(detail);
        }

        // Calculate shipping fee
        BigDecimal shippingFee = calculateShippingFee(subtotal);

        // Apply promotion if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        Promotion promotion = null;
        if (request.getPromotionCode() != null) {
            PromotionApplyResponse promoResponse = promotionService.applyPromotion(
                    request.getPromotionCode(),
                    customer.getCustomerId(),
                    subtotal
            );
            if (promoResponse.getSuccess()) {
                discountAmount = promoResponse.getDiscountAmount();
                promotion = promotionRepository.findById(promoResponse.getPromotionId())
                        .orElse(null);
            }
        }

        // Apply loyalty points
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;
        if (request.getLoyaltyPointsUsed() != null && request.getLoyaltyPointsUsed() > 0) {
            // 1 point = 1000 VND
            loyaltyDiscount = new BigDecimal(request.getLoyaltyPointsUsed() * 1000);
            customerService.redeemLoyaltyPoints(customer.getCustomerId(), request.getLoyaltyPointsUsed());
        }

        // Calculate total
        BigDecimal totalPrice = subtotal
                .add(shippingFee)
                .subtract(discountAmount)
                .subtract(loyaltyDiscount);

        // Generate order code
        String orderCode = generateOrderCode();

        // Create order
        Order order = Order.builder()
                .orderCode(orderCode)
                .customer(customer)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .loyaltyPointsUsed(request.getLoyaltyPointsUsed())
                .loyaltyDiscount(loyaltyDiscount)
                .totalPrice(totalPrice)
                .promotion(promotion)
                .paymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(Order.PaymentStatus.UNPAID)
                .status(Order.OrderStatus.PENDING)
                .note(request.getNote())
                .build();

        order = orderRepository.save(order);

        // Save order details
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(order);
            orderDetailRepository.save(detail);

            // Update stock
            MedicalDevice device = detail.getDevice();
            device.setStockQuantity(device.getStockQuantity() - detail.getQuantity());
            device.setSoldCount(device.getSoldCount() + detail.getQuantity());
            deviceRepository.save(device);
        }

        // Clear customer's cart
        cartRepository.deleteByCustomer(customer);

        return convertToOrderResponse(order, orderDetails);
    }
    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        return convertToOrderResponse(order, details);
    }

    public List<OrderResponse> getCustomerOrders(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer)
                .stream()
                .map(order -> {
                    List<OrderDetail> details = orderDetailRepository.findByOrder(order);
                    return convertToOrderResponse(order, details);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void updateOrderStatus(Integer orderId, String newStatus, Integer employeeId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Order.OrderStatus oldStatus = order.getStatus();
        Order.OrderStatus status = Order.OrderStatus.valueOf(newStatus);

        order.setStatus(status);

        // Update timestamps based on status
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case CONFIRMED:
                order.setConfirmedAt(now);
                order.setConfirmedBy(employee);
                break;
            case PREPARING:
                order.setPreparedAt(now);
                break;
            case SHIPPING:
                order.setShippedAt(now);
                break;
            case COMPLETED:
                order.setCompletedAt(now);
                order.setPaymentStatus(Order.PaymentStatus.PAID);

                // Update customer stats (handled by trigger in entity)
                break;
            case CANCELLED:
                order.setCancelledAt(now);

                // Restore stock
                restoreStock(order);
                break;
        }

        orderRepository.save(order);

        // Create status history
        createOrderStatusHistory(order, oldStatus, status, employee);

        // Send notification to customer
        notificationService.sendOrderStatusNotification(order);
    }
    @Override
    public void createOrderStatusHistory(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus, Employee employee) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(employee)
                .note("Trạng thái đơn hàng thay đổi từ " + oldStatus + " sang " + newStatus)
                .build();

        orderStatusHistoryRepository.save(history);
    }
    @Override
    public void restoreStock(Order order) {
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        for (OrderDetail detail : details) {
            MedicalDevice device = detail.getDevice();
            device.setStockQuantity(device.getStockQuantity() + detail.getQuantity());
            device.setSoldCount(device.getSoldCount() - detail.getQuantity());
            deviceRepository.save(device);
        }
    }
    @Override
    public BigDecimal calculateShippingFee(BigDecimal subtotal) {
        BigDecimal freeShippingThreshold = new BigDecimal("500000");
        BigDecimal defaultShippingFee = new BigDecimal("30000");

        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return defaultShippingFee;
    }
    //tạo mã đơn hàng
    @Override
    public String generateOrderCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + dateStr + randomStr;
    }
    @Override
    public List<OrderResponse> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(PageRequest.of(0, limit))
                .stream()
                .map(order -> {
                    List<OrderDetail> details = orderDetailRepository.findByOrder(order);
                    return convertToOrderResponse(order, details);
                })
                .collect(Collectors.toList());
    }
    @Override
    public OrderResponse convertToOrderResponse(Order order, List<OrderDetail> details) {
        List<OrderDetailDTO> detailDTOs = details.stream()
                .map(d -> OrderDetailDTO.builder()
                        .deviceId(d.getDevice().getDeviceId())
                        .deviceName(d.getDeviceName())
                        .deviceImage(d.getDeviceImage())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .totalPrice(d.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomer().getFullName())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .loyaltyDiscount(order.getLoyaltyDiscount())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .items(detailDTOs)
                .build();
    }
}

