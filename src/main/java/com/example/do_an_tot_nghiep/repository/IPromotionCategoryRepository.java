package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.PromotionCategory;
import com.example.do_an_tot_nghiep.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IPromotionCategoryRepository extends JpaRepository<PromotionCategory, Integer> {

    /**
     * Lấy danh sách danh mục trong khuyến mãi theo Promotion object
     */
    List<PromotionCategory> findByPromotion(Promotion promotion);

    /**
     * Kiểm tra danh mục có trong khuyến mãi không
     */
    @Query("SELECT COUNT(pc) > 0 FROM PromotionCategory pc " +
            "WHERE pc.promotion = :promotion " +
            "AND pc.category.categoryId = :categoryId")
    boolean existsByPromotionAndCategoryId(@Param("promotion") Promotion promotion,
                                           @Param("categoryId") Long categoryId);

    /**
     * Lấy tất cả khuyến mãi của danh mục theo categoryId
     */
    @Query("SELECT pc FROM PromotionCategory pc WHERE pc.category.categoryId = :categoryId")
    List<PromotionCategory> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Lấy khuyến mãi đang hoạt động của danh mục
     */
    @Query("SELECT pc FROM PromotionCategory pc " +
            "WHERE pc.category.categoryId = :categoryId " +
            "AND pc.promotion.isActive = true " +
            "AND pc.promotion.startDate <= :now " +
            "AND pc.promotion.endDate >= :now")
    List<PromotionCategory> findActivePromotionsByCategory(@Param("categoryId") Long categoryId,
                                                           @Param("now") LocalDateTime now);

    /**
     * Xóa tất cả khuyến mãi danh mục theo promotion
     */
    void deleteByPromotion(Promotion promotion);

    /**
     * Đếm số lượng danh mục trong khuyến mãi
     */
    long countByPromotion(Promotion promotion);

    /**
     * Kiểm tra danh mục có bất kỳ khuyến mãi nào không
     */
    @Query("SELECT COUNT(pc) > 0 FROM PromotionCategory pc WHERE pc.category.categoryId = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);
}