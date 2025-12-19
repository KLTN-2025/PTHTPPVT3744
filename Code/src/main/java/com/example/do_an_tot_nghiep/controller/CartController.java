package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import com.example.do_an_tot_nghiep.security.CustomerUserDetails;
import com.example.do_an_tot_nghiep.security.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private IMedicalDeviceRepository medicalDeviceRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ICustomerAddressRepository customerAddressRepository;

    @Autowired
    private IPromotionRepository promotionRepository;

    /**
     * Hiển thị trang giỏ hàng
     */
    @GetMapping({"", "/"})
    public String viewCart(Model model) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer != null) {
                // Khách hàng đã đăng nhập - Lấy giỏ hàng từ database
                List<Cart> cartItems = cartRepository.findByCustomer(customer);

                // Tính tổng tiền
                BigDecimal subtotal = calculateSubtotal(cartItems);
                BigDecimal shippingFee = calculateShippingFee(subtotal);
                BigDecimal total = subtotal.add(shippingFee);

                // Lấy địa chỉ giao hàng
                List<CustomerAddress> addresses = customerAddressRepository.findByCustomerOrderByDefault(customer);
                CustomerAddress defaultAddress = addresses.isEmpty() ? null : addresses.get(0);

                model.addAttribute("cartItems", cartItems);
                model.addAttribute("addresses", addresses);
                model.addAttribute("defaultAddress", defaultAddress);
                model.addAttribute("subtotal", subtotal);
                model.addAttribute("shippingFee", shippingFee);
                model.addAttribute("total", total);
                model.addAttribute("isLoggedIn", true);
                model.addAttribute("customer", customer);
            } else {
                // Khách vãng lai - Sử dụng sessionStorage từ frontend
                model.addAttribute("cartItems", new ArrayList<>());
                model.addAttribute("isLoggedIn", false);
            }

            model.addAttribute("title", "Giỏ hàng - Vật Tư Y Tế ABC");

        } catch (Exception e) {
            System.err.println("Error loading cart: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải giỏ hàng!");
        }

        return "cart/cart";
    }

    /**
     * API: Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            String deviceId = (String) request.get("deviceId");
            Integer quantity = request.get("quantity") != null ?
                    (Integer) request.get("quantity") : 1;

            Customer customer = getCurrentCustomer();

            if (customer == null) {
                // Khách vãng lai - Trả về success để frontend xử lý
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã thêm vào giỏ hàng (session)",
                        "needsSync", false
                ));
            }

            // Khách hàng đã đăng nhập - Lưu vào database
            MedicalDevice device = medicalDeviceRepository.findById(deviceId).orElse(null);

            if (device == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Sản phẩm không tồn tại"
                ));
            }

            // Kiểm tra sản phẩm đã có trong giỏ chưa
            Optional<Cart> existingCart = cartRepository.findByCustomerAndDevice(customer, device);

            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();
                cart.setQuantity(cart.getQuantity() + quantity);
                cartRepository.save(cart);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã cập nhật số lượng: " + cart.getQuantity(),
                        "cartId", cart.getCartId(),
                        "quantity", cart.getQuantity()
                ));
            } else {
                Cart newCart = Cart.builder()
                        .customer(customer)
                        .device(device)
                        .quantity(quantity)
                        .build();
                cartRepository.save(newCart);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã thêm vào giỏ hàng",
                        "cartId", newCart.getCartId()
                ));
            }

        } catch (Exception e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong giỏ
     */
    @PutMapping("/update/{cartId}")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(
            @PathVariable Integer cartId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");

            if (quantity == null || quantity < 1) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng không hợp lệ"
                ));
            }

            Customer customer = getCurrentCustomer();
            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Cart cart = cartRepository.findById(cartId).orElse(null);

            if (cart == null || !cart.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy sản phẩm trong giỏ hàng"
                ));
            }

            // Kiểm tra tồn kho
            if (quantity > cart.getDevice().getStockQuantity()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Số lượng vượt quá tồn kho (" + cart.getDevice().getStockQuantity() + ")"
                ));
            }

            cart.setQuantity(quantity);
            cartRepository.save(cart);

            // Tính lại tổng tiền
            List<Cart> cartItems = cartRepository.findByCustomer(customer);
            BigDecimal subtotal = calculateSubtotal(cartItems);
            BigDecimal shippingFee = calculateShippingFee(subtotal);
            BigDecimal total = subtotal.add(shippingFee);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã cập nhật số lượng",
                    "quantity", quantity,
                    "subtotal", subtotal,
                    "shippingFee", shippingFee,
                    "total", total
            ));

        } catch (Exception e) {
            System.err.println("Error updating cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra"
            ));
        }
    }

    /**
     * API: Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/remove/{cartId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Integer cartId) {
        try {
            Customer customer = getCurrentCustomer();
            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            Cart cart = cartRepository.findById(cartId).orElse(null);

            if (cart == null || !cart.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy sản phẩm trong giỏ hàng"
                ));
            }

            cartRepository.delete(cart);

            // Tính lại tổng tiền
            List<Cart> cartItems = cartRepository.findByCustomer(customer);
            BigDecimal subtotal = calculateSubtotal(cartItems);
            BigDecimal shippingFee = calculateShippingFee(subtotal);
            BigDecimal total = subtotal.add(shippingFee);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                    "cartCount", cartItems.size(),
                    "subtotal", subtotal,
                    "shippingFee", shippingFee,
                    "total", total
            ));

        } catch (Exception e) {
            System.err.println("Error removing from cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra"
            ));
        }
    }

    /**
     * API: Đồng bộ giỏ hàng từ sessionStorage khi đăng nhập
     */
    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<?> syncCart(@RequestBody List<Map<String, Object>> sessionCartItems) {
        try {
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            int addedCount = 0;
            int updatedCount = 0;

            for (Map<String, Object> item : sessionCartItems) {
                String deviceId = (String) item.get("id");
                Integer quantity = (Integer) item.get("quantity");

                MedicalDevice device = medicalDeviceRepository.findById(deviceId).orElse(null);
                if (device == null) continue;

                Optional<Cart> existingCart = cartRepository.findByCustomerAndDevice(customer, device);

                if (existingCart.isPresent()) {
                    Cart cart = existingCart.get();
                    cart.setQuantity(cart.getQuantity() + quantity);
                    cartRepository.save(cart);
                    updatedCount++;
                } else {
                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .device(device)
                            .quantity(quantity)
                            .build();
                    cartRepository.save(newCart);
                    addedCount++;
                }
            }

            List<Cart> cartItems = cartRepository.findByCustomer(customer);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã đồng bộ giỏ hàng: " + addedCount + " mới, " + updatedCount + " cập nhật",
                    "cartCount", cartItems.size()
            ));

        } catch (Exception e) {
            System.err.println("Error syncing cart: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi xảy ra khi đồng bộ giỏ hàng"
            ));
        }
    }

    /**
     * API: Áp dụng mã giảm giá
     */
    @PostMapping("/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, String> request) {
        try {
            String couponCode = request.get("code");
            Customer customer = getCurrentCustomer();

            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "Vui lòng đăng nhập"
                ));
            }

            // Tìm mã giảm giá (đã check active và thời gian)
            Promotion promotion = promotionRepository.findByCode(couponCode, java.time.LocalDateTime.now());

            if (promotion == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mã giảm giá không tồn tại hoặc đã hết hạn"
                ));
            }

            // Kiểm tra giới hạn sử dụng
            if (promotion.getUsageLimit() != null &&
                    promotion.getUsedCount() != null &&
                    promotion.getUsedCount() >= promotion.getUsageLimit()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mã giảm giá đã hết lượt sử dụng"
                ));
            }

            // Tính giảm giá
            List<Cart> cartItems = cartRepository.findByCustomer(customer);
            BigDecimal subtotal = calculateSubtotal(cartItems);

            if (subtotal.compareTo(promotion.getMinOrderAmount()) < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng tối thiểu " + promotion.getMinOrderAmount() + " đ"
                ));
            }

            BigDecimal discountAmount = calculateDiscount(subtotal, promotion);
            BigDecimal shippingFee = promotion.getDiscountType() == Promotion.DiscountType.FREESHIP ?
                    BigDecimal.ZERO : calculateShippingFee(subtotal);
            BigDecimal total = subtotal.subtract(discountAmount).add(shippingFee);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã áp dụng mã giảm giá",
                    "promotionId", promotion.getPromotionId(),
                    "discountAmount", discountAmount,
                    "shippingFee", shippingFee,
                    "total", total
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

    // ==================== HELPER METHODS ====================

    /**
     * Lấy thông tin Customer hiện tại
     */
    private Customer getCurrentCustomer() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            // Customer login bằng username/password
            if (principal instanceof CustomerUserDetails) {
                return ((CustomerUserDetails) principal).getCustomer();
            }

            // Customer login bằng OAuth2 (Google, Facebook)
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
     * Tính tổng tiền giỏ hàng (subtotal)
     */
    private BigDecimal calculateSubtotal(List<Cart> cartItems) {
        return cartItems.stream()
                .map(cart -> {
                    MedicalDevice device = cart.getDevice();
                    BigDecimal price = device.getPrice();

                    // Áp dụng giảm giá của sản phẩm
                    if (device.getDiscountPercent() != null && device.getDiscountPercent() > 0) {
                        BigDecimal discount = price.multiply(
                                BigDecimal.valueOf(device.getDiscountPercent()).divide(BigDecimal.valueOf(100))
                        );
                        price = price.subtract(discount);
                    }

                    return price.multiply(BigDecimal.valueOf(cart.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Tính phí vận chuyển
     */
    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        // Miễn phí ship với đơn >= 500,000đ
        if (subtotal.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(30000); // Phí ship mặc định
    }

    /**
     * Tính số tiền giảm từ promotion
     */
    private BigDecimal calculateDiscount(BigDecimal subtotal, Promotion promotion) {
        BigDecimal discount = BigDecimal.ZERO;

        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENT) {
            discount = subtotal.multiply(
                    promotion.getDiscountValue().divide(BigDecimal.valueOf(100))
            );

            // Giới hạn giảm tối đa
            if (promotion.getMaxDiscountAmount() != null &&
                    discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                discount = promotion.getMaxDiscountAmount();
            }
        } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED) {
            discount = promotion.getDiscountValue();
        }

        return discount;
    }
}