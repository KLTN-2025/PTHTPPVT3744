package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.CategoryDTO;
import com.example.do_an_tot_nghiep.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý quản lý danh mục trong admin
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {

    private final ICategoryService categoryService;

    /**
     * Hiển thị danh sách danh mục
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            Model model) {

        log.info("Accessing category list page - page: {}, size: {}, search: {}, isActive: {}",
                page, size, search, isActive);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));

        Page<CategoryDTO> categoriesPage;

        if (search != null && !search.trim().isEmpty()) {
            categoriesPage = categoryService.searchCategories(search, pageable);
        } else if (isActive != null) {
            categoriesPage = categoryService.getCategoriesByStatus(isActive, pageable);
        } else {
            categoriesPage = categoryService.getAllCategoriesPage(pageable);
        }

        model.addAttribute("categories", categoriesPage.getContent());
        model.addAttribute("currentPage", categoriesPage.getNumber());
        model.addAttribute("totalPages", categoriesPage.getTotalPages());
        model.addAttribute("totalItems", categoriesPage.getTotalElements());
        model.addAttribute("searchKeyword", search);
        model.addAttribute("isActive", isActive);

        return "category/category-list";
    }

    /**
     * Hiển thị form thêm danh mục mới
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("Accessing add category form");

        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("parentCategories", categoryService.getAllParentCategories());
        model.addAttribute("isEdit", false);

        return "category/category-form";
    }

    /**
     * Xử lý thêm danh mục mới
     */
    @PostMapping("/add")
    public String addCategory(
            @ModelAttribute("category") CategoryDTO category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        try {
            log.info("Creating new category: {}", category.getName());
            categoryService.createCategory(category, imageFile);
            return "redirect:/admin/categories?success=created";

        } catch (Exception e) {
            log.error("Error creating category", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", categoryService.getAllParentCategories());
            model.addAttribute("isEdit", false);
            return "category/category-form";
        }
    }

    /**
     * Hiển thị form sửa danh mục
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        log.info("Accessing edit category form for id: {}", id);

        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", categoryService.getAllParentCategories());
            model.addAttribute("isEdit", true);
            return "category/category-form";

        } catch (Exception e) {
            log.error("Error loading category for edit", e);
            return "redirect:/admin/categories?error=notfound";
        }
    }

    /**
     * Xử lý cập nhật danh mục
     */
    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable Integer id,
            @ModelAttribute("category") CategoryDTO category,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        try {
            log.info("Updating category id: {}", id);
            categoryService.updateCategory(id, category, imageFile);
            return "redirect:/admin/categories?success=updated";

        } catch (Exception e) {
            log.error("Error updating category", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("category", category);
            model.addAttribute("parentCategories", categoryService.getAllParentCategories());
            model.addAttribute("isEdit", true);
            return "category/category-form";
        }
    }

    /**
     * Xóa danh mục đơn
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            log.info("Deleting category id: {}", id);
            categoryService.deleteCategory(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa danh mục thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting category", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Xóa nhiều danh mục
     */
    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<?> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            log.info("Deleting multiple categories: {}", ids);
            int deletedCount = categoryService.deleteCategories(ids);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Đã xóa " + deletedCount + " danh mục");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting multiple categories", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Bật/tắt trạng thái danh mục
     */
    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            log.info("Toggling status for category id: {}", id);
            categoryService.toggleStatus(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error toggling category status", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Lấy danh mục con theo parent ID (API)
     */
    @GetMapping("/subcategories/{parentId}")
    @ResponseBody
    public ResponseEntity<?> getSubcategories(@PathVariable Integer parentId) {
        try {
            log.info("Getting subcategories for parent id: {}", parentId);
            List<CategoryDTO> subcategories = categoryService.getSubcategories(parentId);
            return ResponseEntity.ok(subcategories);

        } catch (Exception e) {
            log.error("Error getting subcategories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kiểm tra slug có tồn tại không (AJAX)
     */
    @GetMapping("/check-slug")
    @ResponseBody
    public ResponseEntity<?> checkSlug(
            @RequestParam String slug,
            @RequestParam(required = false) Integer excludeId) {

        boolean exists = categoryService.isSlugExists(slug, excludeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}