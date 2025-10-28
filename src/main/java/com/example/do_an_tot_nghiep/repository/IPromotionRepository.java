package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String promotionCode);
    /**
     * Lấy danh sách khuyến mãi đang hoạt động
     */
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now) " +
            "ORDER BY p.createdAt DESC")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Tìm khuyến mãi theo code
     */
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.code = :code AND p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now)")
    Promotion findByCode(@Param("code") String code, @Param("now") LocalDateTime now);
}

/**
 * BannerRepository - Quản lý banner quảng cáo
 */

