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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {
    @Autowired
    private  IPromotionRepository promotionRepository;
    @Autowired
    private IPromotionUsageRepository promotionUsageRepository;
    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private IOrderRepository orderRepository;

    public PromotionApplyResponse applyPromotion(String promotionCode,
                                                 Integer customerId,
                                                 BigDecimal orderAmount) {
        // Find promotion
        Optional<Promotion> promoOpt = promotionRepository.findByCodeAndIsActiveTrue(promotionCode);

        if (!promoOpt.isPresent()) {
            return PromotionApplyResponse.builder()
                    .success(false)
                    .message("Mã khuyến mãi không tồn tại hoặc đã hết hạn")
                    .discountAmount(BigDecimal.ZERO)
                    .build();
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
        if (!promotion.getCustomerTier().equals("All") &&
                !customer.getCustomerTier().name().equals(promotion.getCustomerTier())) {
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

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderAmount) {
        BigDecimal discount;

        if (promotion.getDiscountType().equals("Percent")) {
            discount = orderAmount.multiply(promotion.getDiscountValue())
                    .divide(new BigDecimal("100"));

            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        } else if (promotion.getDiscountType().equals("Fixed")) {
            discount = promotion.getDiscountValue();
        } else { // FreeShip
            discount = new BigDecimal("30000"); // Default shipping fee
        }

        return discount;
    }

    private PromotionApplyResponse buildFailureResponse(String message) {
        return PromotionApplyResponse.builder()
                .success(false)
                .message(message)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

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
}
