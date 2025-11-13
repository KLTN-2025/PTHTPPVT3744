package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.MedicalDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMedicalDeviceRepository extends JpaRepository<MedicalDevice, String> {

    @Query("SELECT d FROM MedicalDevice d WHERE d.stockQuantity <= d.minStockLevel " +
            "AND d.status != 'Ngừng bán' ORDER BY (d.minStockLevel - d.stockQuantity) DESC")
    List<MedicalDevice> findLowStockProducts();

    @Query("SELECT COUNT(d) FROM MedicalDevice d WHERE d.stockQuantity <= d.minStockLevel")
    Long countLowStockProducts();

    List<MedicalDevice> findByIsFeaturedTrue();

    List<MedicalDevice> findByIsNewTrue();

    @Query("SELECT d FROM MedicalDevice d ORDER BY d.soldCount DESC")
    List<MedicalDevice> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);

    /**
     * Lấy sản phẩm nổi bật, sắp xếp theo lượt xem
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE md.isFeatured = true AND md.status = 'Còn_hàng' " +
            "ORDER BY md.viewCount DESC")
    List<MedicalDevice> findFeaturedProducts();

    /**
     * Lấy top 4 sản phẩm mới, sắp xếp theo ngày tạo
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE md.isNew = true AND md.status = 'Còn_hàng' " +
            "ORDER BY md.createdAt DESC")
    List<MedicalDevice> findTop4NewProducts();

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE LOWER(md.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(md.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicalDevice> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Tìm sản phẩm theo danh mục - SỬA VERSION
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE c.categoryId = :categoryId AND md.status = 'Còn_hàng' " +
            "ORDER BY md.createdAt DESC")
    List<MedicalDevice> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Tìm sản phẩm theo thương hiệu
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE b.brandId = :brandId AND md.status = 'Còn_hàng' " +
            "ORDER BY md.createdAt DESC")
    List<MedicalDevice> findByBrandId(@Param("brandId") Integer brandId);

    /**
     * Lấy tất cả sản phẩm hoạt động
     */
    @Query("SELECT md FROM MedicalDevice md " +
            "LEFT JOIN FETCH md.brand b " +
            "LEFT JOIN FETCH md.category c " +
            "WHERE md.status != 'Ngừng_bán' " +
            "ORDER BY md.createdAt DESC")
    List<MedicalDevice> findAllActive();
    Page<MedicalDevice> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name, String sku, Pageable pageable
    );

    Page<MedicalDevice> findByStatus(MedicalDevice.DeviceStatus status, Pageable pageable);

    Page<MedicalDevice> findByBrandBrandId(Integer brandId, Pageable pageable);

    Page<MedicalDevice> findByCategoryCategoryId(Integer categoryId, Pageable pageable);
}