package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IBannerRepository extends JpaRepository<Banner, Integer> {
    /**
     * Lấy banner đang hoạt động cho trang chủ
     */
    @Query("SELECT b FROM Banner b " +
            "WHERE b.isActive = true " +
            "AND b.position = 'Home_Slider' " +
            "AND (b.startDate IS NULL OR b.startDate <= :now) " +
            "AND (b.endDate IS NULL OR b.endDate >= :now) " +
            "ORDER BY b.displayOrder ASC")
    List<Banner> findActiveBanners(@Param("now") LocalDateTime now);

    /**
     * Lấy banner theo vị trí
     */
    @Query("SELECT b FROM Banner b " +
            "WHERE b.isActive = true " +
            "AND b.position = :position " +
            "AND (b.startDate IS NULL OR b.startDate <= :now) " +
            "AND (b.endDate IS NULL OR b.endDate >= :now) " +
            "ORDER BY b.displayOrder ASC")
    List<Banner> findByPosition(@Param("position") String position, @Param("now") LocalDateTime now);
}

/**
 * BlogPostRepository - Quản lý bài viết blog
 */

