package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CategoryRepository - Quản lý danh mục sản phẩm
 */
@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Lấy top 6 danh mục đang hoạt động, sắp xếp theo thứ tự hiển thị
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findTop6ByIsActiveTrueOrderByDisplayOrder();

    /**
     * Alternative method nếu không cần custom query
     */
    List<Category> findTop6ByIsActiveTrueOrderByDisplayOrderAsc();
}

/**
 * MedicalDeviceRepository - Quản lý thiết bị y tế
 */