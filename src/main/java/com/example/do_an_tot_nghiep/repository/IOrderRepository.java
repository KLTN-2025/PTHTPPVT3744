package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.dto.OrderDetailDTO;
import com.example.do_an_tot_nghiep.dto.OrderStatsDTO;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.customer = :customer " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByCustomerOrderByCreatedAtDesc(@Param("customer") Customer customer);


    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'COMPLETED' " +
            "AND o.completedAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalRevenue(@Param("startDate") java.time.LocalDateTime startDate,
                                         @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE 
            (
                :keyword IS NULL 
                OR o.orderCode LIKE %:keyword%
                OR o.receiverName LIKE %:keyword%
            )
        AND (:status IS NULL OR o.status = :status)
        AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod)
        AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
        AND (:toDate IS NULL OR o.createdAt <= :toDate)
    """)
    Page<Order> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") Order.OrderStatus status,
            @Param("paymentMethod") Order.PaymentMethod paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query(
            value = "SELECT COUNT(*) FROM `order` WHERE status = :status",
            nativeQuery = true
    )
    BigDecimal countByStatus(@Param("status") String status);

    @Query(value = """
    SELECT 
        CAST(COUNT(*) AS UNSIGNED) AS totalOrders,
        CAST(SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS UNSIGNED) AS pendingOrders,
        CAST(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS UNSIGNED) AS completedOrders,
        CAST(SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS UNSIGNED) AS cancelledOrders,
        CAST(SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) AS UNSIGNED) AS returnedOrders
    FROM `order`
""", nativeQuery = true)
    OrderStatsDTO getOrderStats();

    @Query("""
    SELECT new com.example.do_an_tot_nghiep.dto.OrderDetailDTO(
        CONCAT(od.device.deviceId, ''),
        od.deviceName,
        od.deviceImage,
        od.quantity,
        od.unitPrice,
        od.totalPrice
    )
    FROM OrderDetail od
    WHERE od.order.orderId = :orderId
""")
    List<OrderDetailDTO> getOrderItemsByOrderId(Integer orderId);

    Optional<Order> findByOrderCode(String orderCode);
}