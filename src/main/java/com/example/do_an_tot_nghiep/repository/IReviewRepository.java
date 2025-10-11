package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.model.Order;
import com.example.do_an_tot_nghiep.model.Review;
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
}
