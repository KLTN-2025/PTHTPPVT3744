package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Tìm danh mục theo slug
     */
    Category findBySlug(String slug);

    /**
     * Tìm danh mục gốc (không có parent)
     * SỬA: Thay findByParentIdIsNull() thành findByParentIsNull()
     */
    List<Category> findByParentIsNull();

    /**
     * Tìm danh mục theo tên (không phân biệt hoa thường)
     */
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Lọc danh mục theo trạng thái
     */
    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Kiểm tra tên danh mục đã tồn tại
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra slug đã tồn tại
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra có danh mục con không
     * SỬA: Dùng query JPQL vì không có field parentId
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parent.categoryId = :parentId")
    boolean existsByParentId(@Param("parentId") Integer parentId);

    /**
     * Kiểm tra danh mục có sản phẩm không
     */
    @Query("SELECT CASE WHEN COUNT(md) > 0 THEN true ELSE false END " +
            "FROM MedicalDevice md WHERE md.category.categoryId = :categoryId")
    boolean hasProducts(@Param("categoryId") Integer categoryId);

    /**
     * Đếm số sản phẩm trong danh mục
     */
    @Query("SELECT COUNT(md) FROM MedicalDevice md WHERE md.category.categoryId = :categoryId")
    long countProductsByCategory(@Param("categoryId") Integer categoryId);

    /**
     * Lấy danh mục và danh mục con
     */
    @Query("SELECT c FROM Category c WHERE c.parent.categoryId = :parentId ORDER BY c.displayOrder ASC")
    List<Category> findByParentIdOrderByDisplayOrder(@Param("parentId") Integer parentId);

    /**
     * Lấy tất cả danh mục đang hoạt động
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Tìm kiếm danh mục theo nhiều tiêu chí
     */
    @Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive) " +
            "ORDER BY c.displayOrder ASC")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                    @Param("isActive") Boolean isActive,
                                    Pageable pageable);

    /**
     * Lấy top 6 danh mục đang hoạt động
     */
    List<Category> findTop6ByIsActiveTrueOrderByDisplayOrder();

    /**
     * Lấy tất cả danh mục đang hoạt động
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrder();
}