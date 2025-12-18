package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.PromotionProduct;
import com.example.do_an_tot_nghiep.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IPromotionProductRepository extends JpaRepository<PromotionProduct, Integer> {

    /**
     * Lấy danh sách sản phẩm trong khuyến mãi theo Promotion object
     */
    List<PromotionProduct> findByPromotion(Promotion promotion);

    /**
     * Kiểm tra sản phẩm có trong khuyến mãi không (so sánh theo deviceId)
     */
    @Query("SELECT COUNT(pp) > 0 FROM PromotionProduct pp " +
            "WHERE pp.promotion = :promotion " +
            "AND pp.device.deviceId = :deviceId")
    boolean existsByPromotionAndDeviceId(@Param("promotion") Promotion promotion,
                                         @Param("deviceId") String deviceId);

    /**
     * Lấy tất cả khuyến mãi của sản phẩm theo deviceId
     */
    @Query("SELECT pp FROM PromotionProduct pp WHERE pp.device.deviceId = :deviceId")
    List<PromotionProduct> findByDeviceId(@Param("deviceId") String deviceId);

    /**
     * Lấy khuyến mãi đang hoạt động của sản phẩm
     */
    @Query("SELECT pp FROM PromotionProduct pp " +
            "WHERE pp.device.deviceId = :deviceId " +
            "AND pp.promotion.isActive = true " +
            "AND pp.promotion.startDate <= :now " +
            "AND pp.promotion.endDate >= :now")
    List<PromotionProduct> findActivePromotionsByDevice(@Param("deviceId") String deviceId,
                                                        @Param("now") LocalDateTime now);

    /**
     * Lấy danh sách sản phẩm theo danh mục khuyến mãi
     */
    @Query("SELECT pp FROM PromotionProduct pp " +
            "WHERE pp.promotion = :promotion " +
            "AND pp.device.category.categoryId = :categoryId")
    List<PromotionProduct> findByPromotionAndCategoryId(@Param("promotion") Promotion promotion,
                                                        @Param("categoryId") Long categoryId);

    /**
     * Xóa tất cả khuyến mãi sản phẩm theo promotion
     */
    void deleteByPromotion(Promotion promotion);

    /**
     * Đếm số lượng sản phẩm trong khuyến mãi
     */
    long countByPromotion(Promotion promotion);
}