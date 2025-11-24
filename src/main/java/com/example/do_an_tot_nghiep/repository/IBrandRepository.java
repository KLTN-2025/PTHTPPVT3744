package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBrandRepository extends JpaRepository<Brand, Integer> {
    /**
     * Tìm thương hiệu theo tên
     */
    Brand findByName(String name);

    /**
     * Lấy thương hiệu đang hoạt động
     */
    List<Brand> findByIsActiveTrue();
    Brand findBySlug(String slug);

    /**
     * Tìm theo tên (không phân biệt hoa thường)
     */
    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Lọc theo trạng thái
     */
    Page<Brand> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Kiểm tra tên tồn tại
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra slug tồn tại
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra brand có sản phẩm không
     */
    @Query("SELECT CASE WHEN COUNT(md) > 0 THEN true ELSE false END " +
            "FROM MedicalDevice md WHERE md.brand.brandId = :brandId")
    boolean hasProducts(@Param("brandId") Integer brandId);

    /**
     * Đếm số sản phẩm của brand
     */
    @Query("SELECT COUNT(md) FROM MedicalDevice md WHERE md.brand.brandId = :brandId")
    long countProductsByBrand(@Param("brandId") Integer brandId);

    /**
     * Tìm kiếm brands
     */
    @Query("SELECT b FROM Brand b WHERE " +
            "(:keyword IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.country) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR b.isActive = :isActive) " +
            "ORDER BY b.name ASC")
    Page<Brand> searchBrands(@Param("keyword") String keyword,
                             @Param("isActive") Boolean isActive,
                             Pageable pageable);

    /**
     * Lấy tất cả brands đang active
     */
    List<Brand> findByIsActiveTrueOrderByNameAsc();

    /**
     * Lấy brands theo quốc gia
     */
    List<Brand> findByCountryAndIsActiveTrueOrderByNameAsc(String country);
}


