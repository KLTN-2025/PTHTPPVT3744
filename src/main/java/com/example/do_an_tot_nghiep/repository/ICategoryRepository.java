package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Lấy 6 danh mục đang hoạt động, sắp xếp theo thứ tự hiển thị
     */
    List<Category> findTop6ByIsActiveTrueOrderByDisplayOrder();

    /**
     * Lấy tất cả danh mục đang hoạt động, sắp xếp theo thứ tự hiển thị
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * Tìm danh mục theo tên
     */
    Category findByName(String name);

    /**
     * Tìm danh mục theo slug
     */
    Category findBySlug(String slug);

    /**
     * Lấy danh mục con theo parent_id
     */
    List<Category> findByParentCategoryIdOrderByDisplayOrder(Integer parentId);
}