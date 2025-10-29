package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IPromotionRepository - Quản lý khuyến mãi
 */
@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Integer> {

    /**
     * Tìm khuyến mãi theo code (active)
     */
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
     * Tìm khuyến mãi theo code và thời gian hợp lệ
     */
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.code = :code AND p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now)")
    Promotion findByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * Lấy danh sách khuyến mãi theo loại giảm giá
     */
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.isActive = true " +
            "AND p.discountType = :discountType " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now) " +
            "ORDER BY p.endDate ASC")
    List<Promotion> findByDiscountType(@Param("discountType") Promotion.DiscountType discountType,
                                       @Param("now") LocalDateTime now);

    /**
     * Lấy khuyến mãi sắp hết hạn (trong 7 ngày)
     */
    @Query("SELECT p FROM Promotion p " +
            "WHERE p.isActive = true " +
            "AND p.endDate > :now " +
            "AND p.endDate <= :endOfWeek " +
            "ORDER BY p.endDate ASC")
    List<Promotion> findEndingSoon(@Param("now") LocalDateTime now, @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * Lấy tất cả khuyến mãi (active)
     */
    List<Promotion> findByIsActiveTrue();

    /**
     * Tìm khuyến mãi theo ID và active
     */
    Optional<Promotion> findByPromotionIdAndIsActiveTrue(Integer promotionId);

    /**
     * Lấy tất cả khuyến mãi (không phân biệt active)
     */
    @Override
    List<Promotion> findAll();
}