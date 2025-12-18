package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.model.LoyaltyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoyaltyHistoryRepository extends JpaRepository<LoyaltyHistory, Integer> {
    List<LoyaltyHistory> findByCustomerOrderByCreatedAtDesc(Customer customer);

    @Query("SELECT SUM(lh.points) FROM LoyaltyHistory lh WHERE lh.customer = :customer")
    Integer getTotalPointsByCustomer(@Param("customer") Customer customer);

    List<LoyaltyHistory> findByType(LoyaltyHistory.PointType type);

    @Query("SELECT lh FROM LoyaltyHistory lh WHERE lh.customer = :customer " +
            "AND lh.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyHistory> findByCustomerAndDateRange(
            @Param("customer") Customer customer,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}
