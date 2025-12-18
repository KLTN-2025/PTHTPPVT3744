package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.Promotion;
import com.example.do_an_tot_nghiep.model.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPromotionUsageRepository extends JpaRepository<PromotionUsage, Integer> {
    List<PromotionUsage> findByCustomer(Customer customer);

    List<PromotionUsage> findByPromotion(Promotion promotion);

    @Query("SELECT COUNT(pu) FROM PromotionUsage pu WHERE pu.promotion = :promotion AND pu.customer = :customer")
    Long countByPromotionAndCustomer(@Param("promotion") Promotion promotion,
                                     @Param("customer") Customer customer);

    @Query("SELECT pu FROM PromotionUsage pu WHERE pu.usedAt BETWEEN :startDate AND :endDate")
    List<PromotionUsage> findByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                         @Param("endDate") java.time.LocalDateTime endDate);
}
