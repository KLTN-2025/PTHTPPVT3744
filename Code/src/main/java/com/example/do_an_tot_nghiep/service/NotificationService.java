package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import com.example.do_an_tot_nghiep.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    @Autowired
    private INotificationRepository notificationRepository;
    @Autowired
    private ICustomerRepository customerRepository;

    public void sendOrderStatusNotification(Order order) {
        String title = "Cập nhật đơn hàng #" + order.getOrderCode();
        String content = getOrderStatusMessage(order.getStatus());

        Notification notification = Notification.builder()
                .targetType(Notification.TargetType.CUSTOMER)
                .customer(order.getCustomer())
                .title(title)
                .content(content)
                .type(Notification.NotificationType.ORDER)
                .referenceId(order.getOrderId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public void sendPromotionNotification(Customer customer, String promotionInfo) {
        Notification notification = Notification.builder()
                .targetType(Notification.TargetType.CUSTOMER)
                .customer(customer)
                .title("Khuyến mãi đặc biệt")
                .content(promotionInfo)
                .type(Notification.NotificationType.PROMOTION)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> getCustomerNotifications(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return notificationRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private String getOrderStatusMessage(Order.OrderStatus status) {
        switch (status) {
            case CONFIRMED:
                return "Đơn hàng của bạn đã được xác nhận";
            case PREPARING:
                return "Đơn hàng đang được chuẩn bị";
            case SHIPPING:
                return "Đơn hàng đang được giao đến bạn";
            case COMPLETED:
                return "Đơn hàng đã được giao thành công";
            case CANCELLED:
                return "Đơn hàng đã bị hủy";
            default:
                return "Trạng thái đơn hàng đã thay đổi";
        }
    }

    @Override
    public Long getUnreadCountByEmployee(Integer employeeId) {
        return 0L;
    }
}
