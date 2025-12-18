package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByCustomer(Customer customer);
    List<Review> findByDevice(MedicalDevice device);
    List<Review> findByStatus(Review.ReviewStatus status);

    @Query("SELECT r FROM Review r WHERE r.device = :device AND r.status = 'APPROVED' " +
            "ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByDevice(@Param("device") MedicalDevice device);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.device = :device AND r.status = 'APPROVED'")
    Double getAverageRatingByDevice(@Param("device") MedicalDevice device);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.device = :device AND r.status = 'APPROVED'")
    Long countApprovedReviewsByDevice(@Param("device") MedicalDevice device);

    boolean existsByCustomerAndOrder(Customer customer, Order order);

    Page<Review> findByStatus(Review.ReviewStatus status, Pageable pageable);
    Page<Review> findByRating(Integer rating, Pageable pageable);
    Page<Review> findByDevice_DeviceId(Integer deviceId, Pageable pageable);
    Page<Review> findByCustomer_CustomerId(Integer customerId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.device.deviceId = :deviceId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByDevice(@Param("deviceId") Integer deviceId);

    @Query("SELECT r FROM Review r WHERE " +
            "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.customer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.device.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(Review.ReviewStatus status);
    long countByRating(Integer rating);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.device.deviceId = :deviceId AND r.status = 'APPROVED'")
    Double getAverageRatingByDevice(@Param("deviceId") Integer deviceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.device.deviceId = :deviceId AND r.status = 'APPROVED'")
    long countApprovedReviewsByDevice(@Param("deviceId") Integer deviceId);

    /**
     * Kiểm tra customer đã review sản phẩm chưa - PHẢI CÓ @Query
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r " +
            "WHERE r.customer.customerId = :customerId AND r.device.deviceId = :deviceId")
    boolean hasCustomerReviewedProduct(@Param("customerId") Integer customerId,
                                       @Param("deviceId") Integer deviceId);

    @Query("SELECT r FROM Review r WHERE r.adminReply IS NULL AND r.status = 'APPROVED'")
    Page<Review> findReviewsWithoutReply(Pageable pageable);

    Page<Review> findByIsVerifiedPurchase(Boolean isVerifiedPurchase, Pageable pageable);

    @Query("SELECT r.customer.customerId, r.customer.fullName, COUNT(r) as reviewCount " +
            "FROM Review r GROUP BY r.customer.customerId, r.customer.fullName " +
            "ORDER BY reviewCount DESC")
    List<Object[]> findTopReviewers(Pageable pageable);

    boolean existsByOrder_OrderId(Integer orderId);
}