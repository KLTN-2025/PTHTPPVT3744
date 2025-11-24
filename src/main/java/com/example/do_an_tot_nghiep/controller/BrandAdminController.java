package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.BrandDTO;
import com.example.do_an_tot_nghiep.service.IBrandService;
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

@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandAdminController {

    private final IBrandService brandService;

    /**
     * Danh sách brands
     */
    @GetMapping
    public String listBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            Model model) {

        log.info("Accessing brand list - page: {}, search: {}, isActive: {}", page, search, isActive);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<BrandDTO> brandsPage;

        if (search != null && !search.trim().isEmpty()) {
            brandsPage = brandService.searchBrands(search, pageable);
        } else if (isActive != null) {
            brandsPage = brandService.getBrandsByStatus(isActive, pageable);
        } else {
            brandsPage = brandService.getAllBrandsPage(pageable);
        }

        model.addAttribute("brands", brandsPage.getContent());
        model.addAttribute("currentPage", brandsPage.getNumber());
        model.addAttribute("totalPages", brandsPage.getTotalPages());
        model.addAttribute("totalItems", brandsPage.getTotalElements());
        model.addAttribute("searchKeyword", search);
        model.addAttribute("isActive", isActive);

        return "brand/brand-list";
    }

    /**
     * Form thêm brand
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("brand", new BrandDTO());
        model.addAttribute("isEdit", false);
        return "brand/brand-form";
    }

    /**
     * Xử lý thêm brand
     */
    @PostMapping("/add")
    public String addBrand(
            @ModelAttribute("brand") BrandDTO brand,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            Model model) {

        try {
            brandService.createBrand(brand, logoFile);
            return "redirect:/admin/brands?success=created";
        } catch (Exception e) {
            log.error("Error creating brand", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("brand", brand);
            model.addAttribute("isEdit", false);
            return "brand/brand-form";
        }
    }

    /**
     * Form sửa brand
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            BrandDTO brand = brandService.getBrandById(id);
            model.addAttribute("brand", brand);
            model.addAttribute("isEdit", true);
            return "brand/brand-form";
        } catch (Exception e) {
            log.error("Error loading brand", e);
            return "redirect:/admin/brands?error=notfound";
        }
    }

    /**
     * Xử lý cập nhật brand
     */
    @PostMapping("/edit/{id}")
    public String updateBrand(
            @PathVariable Integer id,
            @ModelAttribute("brand") BrandDTO brand,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            Model model) {

        try {
            brandService.updateBrand(id, brand, logoFile);
            return "redirect:/admin/brands?success=updated";
        } catch (Exception e) {
            log.error("Error updating brand", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("brand", brand);
            model.addAttribute("isEdit", true);
            return "brand/brand-form";
        }
    }

    /**
     * Xóa brand
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBrand(@PathVariable Integer id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thương hiệu thành công"));
        } catch (Exception e) {
            log.error("Error deleting brand", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xóa nhiều brands
     */
    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<?> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            int deletedCount = brandService.deleteBrands(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deletedCount", deletedCount,
                    "message", "Đã xóa " + deletedCount + " thương hiệu"
            ));
        } catch (Exception e) {
            log.error("Error deleting brands", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Toggle status
     */
    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            brandService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công"));
        } catch (Exception e) {
            log.error("Error toggling status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Check slug exists
     */
    @GetMapping("/check-slug")
    @ResponseBody
    public ResponseEntity<?> checkSlug(
            @RequestParam String slug,
            @RequestParam(required = false) Integer excludeId) {

        boolean exists = brandService.isSlugExists(slug, excludeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}