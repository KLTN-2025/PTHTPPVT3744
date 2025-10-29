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
    private PromotionService promotionService;  // ✅ Sử dụng Service

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
            // ✅ Gọi Service thay vì Repository
            List<Promotion> promotions = promotionService.getPromotionsByCategory(categoryId);

            // Sắp xếp theo ngày kết thúc (sắp hết trước đến sau)
            promotions.sort((a, b) -> a.getEndDate().compareTo(b.getEndDate()));

            // Phân trang (9 khuyến mãi/trang)
            int pageSize = 9;
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, promotions.size());
            List<Promotion> pagePromotions = promotions.subList(start, Math.max(start, end));

            // Tính số trang
            int totalPages = (int) Math.ceil((double) promotions.size() / pageSize);

            // Lấy danh sách danh mục
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrder();

            // Thêm dữ liệu vào model
            model.addAttribute("promotions", pagePromotions);
            model.addAttribute("categories", categories);
            model.addAttribute("totalPromotions", promotions.size());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("title", "Khuyến mãi - Vật Tư Y Tế ABC");

            System.out.println("Promotions loaded: " + pagePromotions.size());

        } catch (Exception e) {
            System.err.println("Error loading promotions: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("promotions", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải khuyến mãi!");
        }

        return "promotion/promotions";
    }

    /**
     * Hiển thị chi tiết khuyến mãi
     */
    @GetMapping("/{promotionId}")
    public String promotionDetail(@PathVariable Integer promotionId, Model model) {
        try {
            // ✅ Gọi Service
            Optional<Promotion> promotionOpt = promotionService.getPromotionDetail(promotionId);

            if (!promotionOpt.isPresent()) {
                model.addAttribute("errorMessage", "Khuyến mãi không tồn tại hoặc đã hết hạn!");
                return "error/404";
            }

            Promotion promotion = promotionOpt.get();

            // ✅ Lấy sản phẩm và danh mục qua Service
            List<MedicalDevice> products = promotionService.getPromotionProducts(promotion);
            List<Category> categories = promotionService.getPromotionCategories(promotion);

            model.addAttribute("promotion", promotion);
            model.addAttribute("promotionProducts", products);
            model.addAttribute("promotionCategories", categories);
            model.addAttribute("title", promotion.getName() + " - Vật Tư Y Tế ABC");

        } catch (Exception e) {
            System.err.println("Error loading promotion detail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra!");
            return "error/500";
        }

        return "promotion/promotion-detail";
    }
}