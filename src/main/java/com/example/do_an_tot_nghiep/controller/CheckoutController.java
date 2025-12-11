package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import com.example.do_an_tot_nghiep.security.CustomerUserDetails;
import com.example.do_an_tot_nghiep.security.CustomOAuth2User;
import com.example.do_an_tot_nghiep.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ICustomerAddressRepository customerAddressRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private CustomerService customerService;

    // ==================== HIỂN THỊ TRANG CHECKOUT ====================
    @GetMapping({"", "/"})
    public String showCheckout(
            @RequestParam(required = false) String promoCode,
            @RequestParam(required = false) Double discountAmount,
            Model model,
            HttpSession session) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return "redirect:/login?returnUrl=/checkout";
            }

            List<Cart> cartItems = cartRepository.findByCustomer(customer);

            if (cartItems.isEmpty()) {
                model.addAttribute("errorMessage", "Giỏ hàng của bạn đang trống!");
                return "redirect:/cart";
            }

            // Tính toán tổng tiền
            BigDecimal subtotal = calculateSubtotal(cartItems);
            BigDecimal shippingFee = orderService.calculateShippingFee(subtotal);

            // ✅ LẤY MÃ GIẢM GIÁ TỪ SESSION HOẶC PARAM
            String appliedPromoCode = promoCode;
            BigDecimal appliedDiscount = BigDecimal.ZERO;

            // Ưu tiên lấy từ param trước
            if (appliedPromoCode == null && session.getAttribute("promoCode") != null) {
                appliedPromoCode = (String) session.getAttribute("promoCode");
            }

            // Xử lý discount amount
            if (discountAmount != null && discountAmount > 0) {
                appliedDiscount = BigDecimal.valueOf(discountAmount);
                // Lưu vào session để giữ khi reload
                session.setAttribute("promoCode", appliedPromoCode);
                session.setAttribute("discountAmount", appliedDiscount);
            } else if (session.getAttribute("discountAmount") != null) {
                Object sessionDiscount = session.getAttribute("discountAmount");
                if (sessionDiscount instanceof BigDecimal) {
                    appliedDiscount = (BigDecimal) sessionDiscount;
                } else if (sessionDiscount instanceof Double) {
                    appliedDiscount = BigDecimal.valueOf((Double) sessionDiscount);
                }
            }

            BigDecimal total = subtotal.add(shippingFee).subtract(appliedDiscount);
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }

            // Lấy danh sách địa chỉ
            List<CustomerAddress> addresses = customerAddressRepository
                    .findByCustomerOrderByDefault(customer);

            CustomerAddress defaultAddress = addresses.isEmpty() ? null : addresses.get(0);

            // Lấy danh sách khuyến mãi khả dụng
            List<Promotion> availablePromotions = promotionService.getActivePromotions()
                    .stream()
                    .filter(p -> p.getMinOrderAmount().compareTo(subtotal) <= 0)
                    .limit(5)
                    .collect(Collectors.toList());

            // Add attributes to model
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("defaultAddress", defaultAddress);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shippingFee", shippingFee);
            model.addAttribute("discountAmount", appliedDiscount);
            model.addAttribute("appliedPromoCode", appliedPromoCode);
            model.addAttribute("total", total);
            model.addAttribute("customer", customer);
            model.addAttribute("availablePromotions", availablePromotions);
            model.addAttribute("title", "Thanh toán - Vật Tư Y Tế ABC");

            return "checkout/checkout";

        } catch (Exception e) {
            System.err.println("Error loading checkout: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải trang thanh toán!");
            return "redirect:/cart";
        }
    }

    // ==================== API: TẠO ĐƠN HÀNG ====================
    @PostMapping("/place-order")
    @ResponseBody
    public ResponseEntity<?> placeOrder(@RequestBody CheckoutRequest request) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập để tiếp tục"
                ));
            }

            // Validate input
            if (request.getReceiverName() == null || request.getReceiverName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập tên người nhận"
                ));
            }

            if (request.getReceiverPhone() == null || request.getReceiverPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số điện thoại"
                ));
            }

            if (request.getReceiverAddress() == null || request.getReceiverAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập địa chỉ giao hàng"
                ));
            }

            if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng chọn phương thức thanh toán"
                ));
            }

            // Lấy giỏ hàng
            List<Cart> cartItems = cartRepository.findByCustomer(customer);

            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Giỏ hàng của bạn đang trống"
                ));
            }

            // Tạo danh sách items cho OrderRequest
            List<OrderItemRequest> orderItems = cartItems.stream()
                    .map(cart -> {
                        OrderItemRequest item = new OrderItemRequest();
                        item.setDeviceId(cart.getDevice().getDeviceId());
                        item.setQuantity(cart.getQuantity());
                        return item;
                    })
                    .collect(Collectors.toList());

            // Tạo OrderRequest
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCustomerId(customer.getCustomerId());
            orderRequest.setAddressId(request.getAddressId());
            orderRequest.setReceiverName(request.getReceiverName());
            orderRequest.setReceiverPhone(request.getReceiverPhone());
            orderRequest.setReceiverAddress(request.getReceiverAddress());
            orderRequest.setPaymentMethod(request.getPaymentMethod());
            orderRequest.setPromotionCode(request.getPromotionCode());
            orderRequest.setLoyaltyPointsUsed(request.getLoyaltyPointsUsed());
            orderRequest.setNote(request.getNote());
            orderRequest.setItems(orderItems);

            // Tạo đơn hàng
            OrderResponse orderResponse = orderService.createOrder(orderRequest);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đặt hàng thành công!",
                    "orderId", orderResponse.getOrderId(),
                    "orderCode", orderResponse.getOrderCode()
            ));

        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // ==================== API: THÊM ĐỊA CHỈ MỚI ====================
    @PostMapping("/add-address")
    @ResponseBody
    public ResponseEntity<?> addAddress(@RequestBody CustomerAddress addressRequest) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // Validate
            if (addressRequest.getReceiverName() == null || addressRequest.getReceiverName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập tên người nhận"
                ));
            }

            if (addressRequest.getPhone() == null || addressRequest.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập số điện thoại"
                ));
            }

            if (addressRequest.getAddress() == null || addressRequest.getAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Vui lòng nhập địa chỉ"
                ));
            }

            // Tạo địa chỉ mới
            CustomerAddress newAddress = CustomerAddress.builder()
                    .customer(customer)
                    .receiverName(addressRequest.getReceiverName())
                    .phone(addressRequest.getPhone())
                    .address(addressRequest.getAddress())
                    .ward(addressRequest.getWard())
                    .district(addressRequest.getDistrict())
                    .province(addressRequest.getProvince())
                    .isDefault(false)
                    .build();

            newAddress = customerAddressRepository.save(newAddress);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm địa chỉ mới",
                    "addressId", newAddress.getAddressId(),
                    "receiverName", newAddress.getReceiverName(),
                    "phone", newAddress.getPhone(),
                    "fullAddress", buildFullAddress(newAddress)
            ));

        } catch (Exception e) {
            System.err.println("Error adding address: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra khi thêm địa chỉ"
            ));
        }
    }

    // ==================== API: ÁP DỤNG MÃ GIẢM GIÁ ====================
    @PostMapping("/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            String couponCode = (String) request.get("code");

            // ✅ FIX: Xử lý cả Integer và Double
            Object orderAmountObj = request.get("orderAmount");
            BigDecimal orderAmount;

            if (orderAmountObj instanceof Integer) {
                orderAmount = BigDecimal.valueOf(((Integer) orderAmountObj).doubleValue());
            } else if (orderAmountObj instanceof Double) {
                orderAmount = BigDecimal.valueOf((Double) orderAmountObj);
            } else if (orderAmountObj instanceof String) {
                orderAmount = new BigDecimal((String) orderAmountObj);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số tiền không hợp lệ"
                ));
            }

            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            PromotionApplyResponse promoResponse = promotionService.applyPromotion(
                    couponCode,
                    customer.getCustomerId(),
                    orderAmount
            );

            if (!promoResponse.getSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", promoResponse.getMessage()
                ));
            }

            // ✅ LƯU VÀO SESSION để giữ khi chuyển trang
            session.setAttribute("promoCode", couponCode);
            session.setAttribute("discountAmount", promoResponse.getDiscountAmount());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", promoResponse.getMessage(),
                    "discountAmount", promoResponse.getDiscountAmount(),
                    "promotionId", promoResponse.getPromotionId()
            ));

        } catch (Exception e) {
            System.err.println("Error applying coupon: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra"
            ));
        }
    }

    // ==================== API: TÍNH LẠI TỔNG TIỀN ====================
    @PostMapping("/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateTotal(@RequestBody Map<String, Object> request) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            List<Cart> cartItems = cartRepository.findByCustomer(customer);

            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Giỏ hàng trống"
                ));
            }

            BigDecimal subtotal = calculateSubtotal(cartItems);
            BigDecimal shippingFee = orderService.calculateShippingFee(subtotal);

            // Tính giảm giá nếu có
            BigDecimal discountAmount = BigDecimal.ZERO;
            String promotionCode = (String) request.get("promotionCode");

            if (promotionCode != null && !promotionCode.isEmpty()) {
                PromotionApplyResponse promoResponse = promotionService.applyPromotion(
                        promotionCode,
                        customer.getCustomerId(),
                        subtotal
                );

                if (promoResponse.getSuccess()) {
                    discountAmount = promoResponse.getDiscountAmount();
                }
            }

            // Tính giảm giá từ điểm tích lũy
            BigDecimal loyaltyDiscount = BigDecimal.ZERO;
            Integer loyaltyPointsUsed = (Integer) request.get("loyaltyPointsUsed");

            if (loyaltyPointsUsed != null && loyaltyPointsUsed > 0) {
                if (loyaltyPointsUsed > customer.getLoyaltyPoints()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "Bạn chỉ có " + customer.getLoyaltyPoints() + " điểm"
                    ));
                }

                loyaltyDiscount = BigDecimal.valueOf(loyaltyPointsUsed * 1000);
            }

            BigDecimal total = subtotal.add(shippingFee).subtract(discountAmount).subtract(loyaltyDiscount);

            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "subtotal", subtotal,
                    "shippingFee", shippingFee,
                    "discountAmount", discountAmount,
                    "loyaltyDiscount", loyaltyDiscount,
                    "total", total
            ));

        } catch (Exception e) {
            System.err.println("Error calculating total: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra"
            ));
        }
    }

    // ==================== HELPER METHODS ====================

    private Customer getCurrentCustomer() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomerUserDetails) {
                return ((CustomerUserDetails) principal).getCustomer();
            }

            if (principal instanceof CustomOAuth2User) {
                CustomerDTO customerDTO = ((CustomOAuth2User) principal).getCustomer();
                return customerRepository.findById(customerDTO.getCustomerId()).orElse(null);
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error getting current customer: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Tính tổng tiền giỏ hàng (subtotal) - ÁP DỤNG GIẢM GIÁ
     */
    private BigDecimal calculateSubtotal(List<Cart> cartItems) {
        return cartItems.stream()
                .map(cart -> {
                    MedicalDevice device = cart.getDevice();
                    BigDecimal price = device.getPrice();

                    // ✅ Áp dụng giảm giá của sản phẩm
                    if (device.getDiscountPercent() != null && device.getDiscountPercent() > 0) {
                        BigDecimal discountMultiplier = BigDecimal.ONE
                                .subtract(BigDecimal.valueOf(device.getDiscountPercent())
                                        .divide(BigDecimal.valueOf(100)));
                        price = price.multiply(discountMultiplier);
                    }

                    return price.multiply(BigDecimal.valueOf(cart.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildFullAddress(CustomerAddress address) {
        StringBuilder fullAddress = new StringBuilder(address.getAddress());

        if (address.getWard() != null && !address.getWard().isEmpty()) {
            fullAddress.append(", ").append(address.getWard());
        }

        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            fullAddress.append(", ").append(address.getDistrict());
        }

        if (address.getProvince() != null && !address.getProvince().isEmpty()) {
            fullAddress.append(", ").append(address.getProvince());
        }

        return fullAddress.toString();
    }
}