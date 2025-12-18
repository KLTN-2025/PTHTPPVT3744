package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.ICategoryRepository;
import com.example.do_an_tot_nghiep.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ICategoryRepository categoryRepository;

    /**
     * Hiển thị trang danh sách khuyến mãi
     */
    @GetMapping({"", "/"})
    public String promotions(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {
        try {
            List<Promotion> promotions = promotionService.getPromotionsByCategory(categoryId);

            // Sắp xếp theo ngày kết thúc
            promotions.sort((a, b) -> a.getEndDate().compareTo(b.getEndDate()));

            // ✅ THÊM DEBUG
            System.out.println("========== PROMOTION DEBUG ==========");
            for (Promotion p : promotions) {
                System.out.println("ID: " + p.getPromotionId());
                System.out.println("Name: " + p.getName());
                System.out.println("DiscountType: " + p.getDiscountType());
                System.out.println("DiscountType (name): " + p.getDiscountType().name());
                System.out.println("DiscountValue: " + p.getDiscountValue());
                System.out.println("---");
            }
            System.out.println("=====================================");

            // Phân trang
            int pageSize = 9;
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, promotions.size());
            List<Promotion> pagePromotions = promotions.subList(start, Math.max(start, end));

            int totalPages = (int) Math.ceil((double) promotions.size() / pageSize);

            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrder();

            model.addAttribute("promotions", pagePromotions);
            model.addAttribute("categories", categories);
            model.addAttribute("totalPromotions", promotions.size());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("title", "Khuyến mãi - Vật Tư Y Tế ABC");

        } catch (Exception e) {
            System.err.println("Error loading promotions: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("promotions", List.of());
            model.addAttribute("categories", List.of());
        }

        // ✅ SỬA: Đường dẫn view phải khớp với cấu trúc thư mục
        // Nếu file là: resources/templates/promotion/promotions.html
        return "promotion/promotions";

        // Hoặc nếu file là: resources/templates/frontend/promotions/list.html
        // return "frontend/promotions/list";
    }

    /**
     * Hiển thị chi tiết khuyến mãi
     */
    @GetMapping("/{promotionId}")
    public String promotionDetail(@PathVariable Integer promotionId, Model model) {
        try {
            Optional<Promotion> promotionOpt = promotionService.getPromotionDetail(promotionId);

            if (!promotionOpt.isPresent()) {
                model.addAttribute("errorMessage", "Khuyến mãi không tồn tại!");
                return "error/404";
            }

            Promotion promotion = promotionOpt.get();

            // ✅ THÊM DEBUG
            System.out.println("========== PROMOTION DETAIL ==========");
            System.out.println("ID: " + promotion.getPromotionId());
            System.out.println("Name: " + promotion.getName());
            System.out.println("DiscountType: " + promotion.getDiscountType());
            System.out.println("DiscountType (name): " + promotion.getDiscountType().name());
            System.out.println("DiscountValue: " + promotion.getDiscountValue());
            System.out.println("======================================");

            List<MedicalDevice> products = promotionService.getPromotionProducts(promotion);
            List<Category> categories = promotionService.getPromotionCategories(promotion);

            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionProducts", products);
            model.addAttribute("promotionCategories", categories);
            model.addAttribute("title", promotion.getName() + " - Vật Tư Y Tế ABC");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return "error/500";
        }

        // ✅ SỬA: Đường dẫn view
        return "promotion/promotion-detail";
        // Hoặc: return "frontend/promotions/detail";
    }
}