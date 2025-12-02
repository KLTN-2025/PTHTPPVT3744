package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.service.PromotionService;
import com.example.do_an_tot_nghiep.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    @Autowired
    private IPromotionRepository promotionRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IMedicalDeviceRepository deviceRepository;

    @Autowired
    private IPromotionCategoryRepository promotionCategoryRepository;

    @Autowired
    private IPromotionProductRepository promotionProductRepository;

    @Autowired
    private IPromotionUsageRepository promotionUsageRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Hiển thị danh sách khuyến mãi
     */
    @GetMapping({"", "/"})
    public String listPromotions(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        List<Promotion> promotions = promotionRepository.findAll();

        // Filter by search
        if (search != null && !search.isEmpty()) {
            promotions = promotions.stream()
                    .filter(p -> p.getCode().toLowerCase().contains(search.toLowerCase()) ||
                            p.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            if ("active".equals(status)) {
                promotions = promotions.stream()
                        .filter(p -> p.getIsActive() &&
                                (p.getStartDate() == null || p.getStartDate().isBefore(now)) &&
                                (p.getEndDate() == null || p.getEndDate().isAfter(now)))
                        .collect(Collectors.toList());
            } else if ("expired".equals(status)) {
                promotions = promotions.stream()
                        .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now))
                        .collect(Collectors.toList());
            } else if ("upcoming".equals(status)) {
                promotions = promotions.stream()
                        .filter(p -> p.getStartDate() != null && p.getStartDate().isAfter(now))
                        .collect(Collectors.toList());
            } else if ("inactive".equals(status)) {
                promotions = promotions.stream()
                        .filter(p -> !p.getIsActive())
                        .collect(Collectors.toList());
            }
        }

        // Sort by created date desc
        promotions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        model.addAttribute("promotions", promotions);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("totalPromotions", promotions.size());

        return "admin/promotions/list";
    }

    /**
     * Hiển thị form tạo khuyến mãi mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Promotion promotion = new Promotion();
        // Set default dates để tránh null trong form
        LocalDateTime now = LocalDateTime.now();
        promotion.setStartDate(now);
        promotion.setEndDate(now.plusDays(30));

        model.addAttribute("promotion", promotion);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("products", deviceRepository.findAll());
        model.addAttribute("discountTypes", Promotion.DiscountType.values());
        model.addAttribute("customerTiers", Promotion.CustomerTier.values());
        model.addAttribute("applicableToOptions", Promotion.ApplicableTo.values());

        // Add formatted dates
        model.addAttribute("startDateFormatted", promotion.getStartDate().format(DATETIME_FORMATTER));
        model.addAttribute("endDateFormatted", promotion.getEndDate().format(DATETIME_FORMATTER));

        return "admin/promotions/form";
    }

    /**
     * Hiển thị form chỉnh sửa khuyến mãi
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(id);

        if (!promotionOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Khuyến mãi không tồn tại!");
            return "redirect:/admin/promotions";
        }

        Promotion promotion = promotionOpt.get();

        // Load categories và products đã chọn
        List<Category> selectedCategories = promotionCategoryRepository.findByPromotion(promotion)
                .stream()
                .map(PromotionCategory::getCategory)
                .collect(Collectors.toList());

        List<MedicalDevice> selectedProducts = promotionProductRepository.findByPromotion(promotion)
                .stream()
                .map(PromotionProduct::getDevice)
                .collect(Collectors.toList());

        model.addAttribute("promotion", promotion);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("products", deviceRepository.findAll());
        model.addAttribute("selectedCategories", selectedCategories);
        model.addAttribute("selectedProducts", selectedProducts);
        model.addAttribute("discountTypes", Promotion.DiscountType.values());
        model.addAttribute("customerTiers", Promotion.CustomerTier.values());
        model.addAttribute("applicableToOptions", Promotion.ApplicableTo.values());

        // Add formatted dates
        model.addAttribute("startDateFormatted",
                promotion.getStartDate() != null ? promotion.getStartDate().format(DATETIME_FORMATTER) : "");
        model.addAttribute("endDateFormatted",
                promotion.getEndDate() != null ? promotion.getEndDate().format(DATETIME_FORMATTER) : "");

        return "admin/promotions/form";
    }

    /**
     * Lưu khuyến mãi (Create/Update)
     */
    @PostMapping("/save")
    public String savePromotion(
            @ModelAttribute Promotion promotion,
            @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds,
            @RequestParam(value = "productIds", required = false) List<String> productIds,
            @RequestParam(value = "startDateStr") String startDateStr,
            @RequestParam(value = "endDateStr") String endDateStr,
            @RequestParam(value = "isActive", required = false, defaultValue = "false") Boolean isActive,
            RedirectAttributes redirectAttributes) {

        try {
            // Parse dates
            promotion.setStartDate(LocalDateTime.parse(startDateStr, DATETIME_FORMATTER));
            promotion.setEndDate(LocalDateTime.parse(endDateStr, DATETIME_FORMATTER));

            // Set isActive (checkbox only sends value when checked)
            promotion.setIsActive(isActive);

            // Validate dates
            if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu!");
                return "redirect:/admin/promotions/create";
            }

            // Save promotion
            Promotion savedPromotion = promotionRepository.save(promotion);

            // Clear old relationships if editing
            if (promotion.getPromotionId() != null) {
                promotionCategoryRepository.deleteByPromotion(savedPromotion);
                promotionProductRepository.deleteByPromotion(savedPromotion);
            }

            // Save categories
            if (categoryIds != null && !categoryIds.isEmpty() &&
                    promotion.getApplicableTo() == Promotion.ApplicableTo.Category) {
                for (Integer categoryId : categoryIds) {
                    Category category = categoryRepository.findById((int) categoryId.longValue()).orElse(null);
                    if (category != null) {
                        PromotionCategory pc = PromotionCategory.builder()
                                .promotion(savedPromotion)
                                .category(category)
                                .build();
                        promotionCategoryRepository.save(pc);
                    }
                }
            }

            // Save products
            if (productIds != null && !productIds.isEmpty() &&
                    promotion.getApplicableTo() == Promotion.ApplicableTo.Product) {
                for (String productId : productIds) {
                    MedicalDevice device = deviceRepository.findById(productId).orElse(null);
                    if (device != null) {
                        PromotionProduct pp = PromotionProduct.builder()
                                .promotion(savedPromotion)
                                .device(device)
                                .build();
                        promotionProductRepository.save(pp);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success",
                    promotion.getPromotionId() == null ?
                            "Tạo khuyến mãi thành công!" :
                            "Cập nhật khuyến mãi thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/promotions";
    }

    /**
     * Xem chi tiết khuyến mãi
     */
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(id);

        if (!promotionOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Khuyến mãi không tồn tại!");
            return "redirect:/admin/promotions";
        }

        Promotion promotion = promotionOpt.get();

        // Load usage statistics
        List<PromotionUsage> usages = promotionUsageRepository.findByPromotion(promotion);

        // Load categories
        List<Category> categories = promotionCategoryRepository.findByPromotion(promotion)
                .stream()
                .map(PromotionCategory::getCategory)
                .collect(Collectors.toList());

        // Load products
        List<MedicalDevice> products = promotionProductRepository.findByPromotion(promotion)
                .stream()
                .map(PromotionProduct::getDevice)
                .collect(Collectors.toList());

        model.addAttribute("promotion", promotion);
        model.addAttribute("usages", usages);
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        model.addAttribute("totalUsage", usages.size());

        return "admin/promotions/detail";
    }

    /**
     * Toggle trạng thái active/inactive
     */
    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);

            if (!promotionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Khuyến mãi không tồn tại!"
                ));
            }

            Promotion promotion = promotionOpt.get();
            promotion.setIsActive(!promotion.getIsActive());
            promotionRepository.save(promotion);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", promotion.getIsActive() ?
                            "Đã kích hoạt khuyến mãi!" :
                            "Đã vô hiệu hóa khuyến mãi!",
                    "isActive", promotion.getIsActive()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * Xóa khuyến mãi
     */
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Promotion> promotionOpt = promotionRepository.findById(id);

            if (!promotionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Khuyến mãi không tồn tại!");
                return "redirect:/admin/promotions";
            }

            Promotion promotion = promotionOpt.get();

            // Check if used
            List<PromotionUsage> usages = promotionUsageRepository.findByPromotion(promotion);
            if (!usages.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể xóa khuyến mãi đã được sử dụng!");
                return "redirect:/admin/promotions";
            }

            // Delete relationships first
            promotionCategoryRepository.deleteByPromotion(promotion);
            promotionProductRepository.deleteByPromotion(promotion);

            // Delete promotion
            promotionRepository.delete(promotion);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/promotions";
    }
}