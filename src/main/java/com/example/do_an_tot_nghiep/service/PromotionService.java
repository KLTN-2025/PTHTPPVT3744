package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    @Autowired
    private IPromotionRepository promotionRepository;

    @Autowired
    private IPromotionUsageRepository promotionUsageRepository;

    @Autowired
    private IPromotionProductRepository promotionProductRepository;

    @Autowired
    private IPromotionCategoryRepository promotionCategoryRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IOrderRepository orderRepository;

    // =============================================
    // PHẦN MỚI: XEM KHUYẾN MÃI (CHO FRONTEND)
    // =============================================

    /**
     * Lấy danh sách khuyến mãi đang hoạt động
     */
    public List<Promotion> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActivePromotions(now);
    }

    /**
     * Lấy danh sách khuyến mãi theo danh mục
     */
    public List<Promotion> getPromotionsByCategory(Integer categoryId) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> allPromotions = promotionRepository.findActivePromotions(now);

        if (categoryId == null) {
            return allPromotions;
        }

        return allPromotions.stream()
                .filter(p -> hasCategory(p, categoryId))
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết khuyến mãi
     */
    public Optional<Promotion> getPromotionDetail(Integer promotionId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<Promotion> promotionOpt = promotionRepository.findByPromotionIdAndIsActiveTrue(promotionId);

        if (!promotionOpt.isPresent()) {
            return Optional.empty();
        }

        Promotion promotion = promotionOpt.get();

        // Kiểm tra thời gian hợp lệ
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            return Optional.empty();
        }

        return Optional.of(promotion);
    }

    /**
     * Lấy danh sách sản phẩm trong khuyến mãi
     */
    public List<MedicalDevice> getPromotionProducts(Promotion promotion) {
        List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
        return promotionProducts.stream()
                .map(PromotionProduct::getDevice)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách danh mục trong khuyến mãi
     */
    public List<Category> getPromotionCategories(Promotion promotion) {
        List<PromotionCategory> promotionCategories = promotionCategoryRepository.findByPromotion(promotion);
        return promotionCategories.stream()
                .map(PromotionCategory::getCategory)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra khuyến mãi có áp dụng cho danh mục không
     */
    private boolean hasCategory(Promotion promotion, Integer categoryId) {
        if (categoryId == null || promotion == null) {
            return false;
        }

        // Trường hợp 1: Khuyến mãi áp dụng cho tất cả
        if (promotion.getApplicableTo() == Promotion.ApplicableTo.All) {
            return true;
        }

        // Trường hợp 2: Khuyến mãi áp dụng cho danh mục cụ thể
        if (promotion.getApplicableTo() == Promotion.ApplicableTo.Category) {
            List<PromotionCategory> promotionCategories = promotionCategoryRepository.findByPromotion(promotion);
            return promotionCategories.stream()
                    .anyMatch(pc -> pc.getCategory() != null &&
                            pc.getCategory().getCategoryId().equals(categoryId.longValue()));
        }

        // Trường hợp 3: Khuyến mãi áp dụng cho sản phẩm cụ thể
        if (promotion.getApplicableTo() == Promotion.ApplicableTo.Product) {
            List<PromotionProduct> promotionProducts = promotionProductRepository.findByPromotion(promotion);
            return promotionProducts.stream()
                    .anyMatch(pp -> pp.getDevice() != null &&
                            pp.getDevice().getCategory() != null &&
                            pp.getDevice().getCategory().getCategoryId().equals(categoryId.longValue()));
        }

        return false;
    }

    // =============================================
    // PHẦN CŨ: ÁP DỤNG MÃ GIẢM GIÁ
    // =============================================

    /**
     * Áp dụng mã khuyến mãi
     */
    public PromotionApplyResponse applyPromotion(String promotionCode,
                                                 Integer customerId,
                                                 BigDecimal orderAmount) {
        // Find promotion
        Optional<Promotion> promoOpt = promotionRepository.findByCodeAndIsActiveTrue(promotionCode);

        if (!promoOpt.isPresent()) {
            return buildFailureResponse("Mã khuyến mãi không tồn tại hoặc đã hết hạn");
        }

        Promotion promotion = promoOpt.get();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check date validity
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
            return buildFailureResponse("Mã khuyến mãi chưa có hiệu lực");
        }
        if (promotion.getEndDate() != null && now.isAfter(promotion.getEndDate())) {
            return buildFailureResponse("Mã khuyến mãi đã hết hạn");
        }

        // Check customer tier
        if (!promotion.getCustomerTier().equals(Promotion.CustomerTier.All) &&
                !customer.getCustomerTier().name().equals(promotion.getCustomerTier().name())) {
            return buildFailureResponse("Mã này chỉ dành cho khách hàng hạng " + promotion.getCustomerTier());
        }

        // Check minimum order amount
        if (orderAmount.compareTo(promotion.getMinOrderAmount()) < 0) {
            return buildFailureResponse("Đơn hàng tối thiểu " + promotion.getMinOrderAmount() + " VNĐ");
        }

        // Check usage limit
        if (promotion.getUsageLimit() != null &&
                promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return buildFailureResponse("Mã khuyến mãi đã hết lượt sử dụng");
        }

        // Check customer usage
        Long customerUsage = promotionUsageRepository
                .countByPromotionAndCustomer(promotion, customer);

        if (customerUsage >= promotion.getUsagePerCustomer()) {
            return buildFailureResponse("Bạn đã sử dụng hết số lần cho mã này");
        }

        // Calculate discount
        BigDecimal discountAmount = calculateDiscount(promotion, orderAmount);

        return PromotionApplyResponse.builder()
                .success(true)
                .message("Áp dụng thành công")
                .discountAmount(discountAmount)
                .promotionId(promotion.getPromotionId())
                .build();
    }

    /**
     * Tính toán số tiền giảm giá
     */
    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        BigDecimal discount;

        if (promotion.getDiscountType().equals(Promotion.DiscountType.Percent)) {
            discount = orderAmount.multiply(promotion.getDiscountValue())
                    .divide(new BigDecimal("100"));

            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        } else if (promotion.getDiscountType().equals(Promotion.DiscountType.Fixed)) {
            discount = promotion.getDiscountValue();
        } else { // FreeShip
            discount = new BigDecimal("30000"); // Default shipping fee
        }

        return discount;
    }

    /**
     * Ghi nhận việc sử dụng khuyến mãi
     */
    @Transactional
    public void recordPromotionUsage(Integer promotionId, Integer customerId,
                                     Integer orderId, BigDecimal discountAmount) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        PromotionUsage usage = PromotionUsage.builder()
                .promotion(promotion)
                .customer(customer)
                .order(order)
                .discountAmount(discountAmount)
                .build();

        promotionUsageRepository.save(usage);

        // Update promotion used count
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);
    }

    /**
     * Build failure response
     */
    private PromotionApplyResponse buildFailureResponse(String message) {
        return PromotionApplyResponse.builder()
                .success(false)
                .message(message)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}