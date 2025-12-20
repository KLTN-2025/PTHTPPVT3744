package com.example.do_an_tot_nghiep.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import com.example.do_an_tot_nghiep.security.CustomerUserDetails;
import com.example.do_an_tot_nghiep.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    /**
     * ============================================
     * HELPER METHOD - Lấy Customer từ Principal
     * Hỗ trợ cả LOCAL login và OAuth2 login
     * ============================================
     */
    private Customer getCustomerFromPrincipal(Object principal) {
        Customer customer = null;

        if (principal instanceof CustomerUserDetails) {
            // Đăng nhập LOCAL (username/password)
            customer = ((CustomerUserDetails) principal).getCustomer();
        } else if (principal instanceof OAuth2User) {
            // Đăng nhập OAuth2 (Google, Facebook, etc.)
            OAuth2User oAuth2User = (OAuth2User) principal;
            String email = oAuth2User.getAttribute("email");

            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Không thể lấy email từ tài khoản OAuth2");
            }

            customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
        }

        if (customer == null) {
            throw new RuntimeException("Không thể xác định thông tin khách hàng");
        }

        return customer;
    }

    /**
     * ============================================
     * VIEW PROFILE
     * ============================================
     */
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login";
            }

            Customer customer;

            // ===== GOOGLE LOGIN =====
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                OAuth2User oauthUser = oauthToken.getPrincipal();
                String email = oauthUser.getAttribute("email");

                if (email == null) {
                    throw new RuntimeException("Không lấy được email từ Google");
                }

                customer = customerRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng Google"));
            }

            // ===== LOCAL LOGIN =====
            else {
                String username = authentication.getName();
                customer = customerRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
            }

            model.addAttribute("customer", customer);
            return "customer/customer-profile";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }



    /**
     * ============================================
     * UPLOAD AVATAR
     * ============================================
     */
    @PostMapping("/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal Object principal) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File không được để trống"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Chỉ chấp nhận file ảnh"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Kích thước file không được vượt quá 5MB"));
            }

            // Get current customer (hỗ trợ cả LOCAL và OAuth2)
            Customer customer = getCustomerFromPrincipal(principal);
            customer = customerRepository.findById(customer.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "customer_avatars",
                    "public_id", "cust_" + customer.getCustomerId() + "_" + UUID.randomUUID(),
                    "overwrite", true,
                    "resource_type", "image"
            ));

            String imageUrl = uploadResult.get("secure_url").toString();

            // Delete old avatar if exists
            if (customer.getAvatarUrl() != null && !customer.getAvatarUrl().isEmpty()) {
                try {
                    String oldPublicId = customer.getAvatarUrl()
                            .substring(customer.getAvatarUrl().lastIndexOf("/") + 1)
                            .split("\\.")[0];
                    cloudinary.uploader().destroy("customer_avatars/" + oldPublicId, ObjectUtils.emptyMap());
                } catch (Exception ignored) {
                    // Ignore error when deleting old avatar
                }
            }

            // Update customer avatar
            customer.setAvatarUrl(imageUrl);
            customerRepository.save(customer);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật ảnh đại diện thành công",
                    "avatarUrl", imageUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi upload: " + e.getMessage()));
        }
    }

    /**
     * ============================================
     * UPDATE PROFILE - ĐÃ SỬA
     * ============================================
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
            @RequestParam(required = false) String gender,
            @AuthenticationPrincipal Object principal,
            RedirectAttributes redirectAttributes) {

        try {
            Customer customer = getCustomerFromPrincipal(principal);
            customer = customerRepository.findById(customer.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // Validate dateOfBirth
            if (dateOfBirth != null) {
                // Kiểm tra ngày trong tương lai
                if (dateOfBirth.isAfter(LocalDate.now())) {
                    redirectAttributes.addFlashAttribute("error",
                            "Ngày sinh không được là ngày trong tương lai!");
                    return "redirect:/customer/profile";
                }

                // Kiểm tra tuổi tối thiểu 13
                LocalDate minDate = LocalDate.now().minusYears(13);
                if (dateOfBirth.isAfter(minDate)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Bạn phải đủ 13 tuổi để sử dụng dịch vụ!");
                    return "redirect:/customer/profile";
                }

                // Kiểm tra tuổi tối đa 150
                LocalDate maxDate = LocalDate.now().minusYears(150);
                if (dateOfBirth.isBefore(maxDate)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Ngày sinh không hợp lệ!");
                    return "redirect:/customer/profile";
                }

                customer.setDateOfBirth(dateOfBirth);
            }

            // Validate gender
            if (gender != null && !gender.isEmpty()) {
                try {
                    Customer.Gender genderEnum = Customer.Gender.valueOf(gender.toUpperCase());
                    customer.setGender(genderEnum);
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", "Giới tính không hợp lệ!");
                    return "redirect:/customer/profile";
                }
            }

            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setAddress(address);

            customerRepository.save(customer);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    /**
     * ============================================
     * CHANGE PASSWORD
     * ============================================
     */
    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal Object principal) {

        try {
            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu mới không khớp"));
            }

            // Validate password strength
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự"));
            }

            // Get current customer (hỗ trợ cả LOCAL và OAuth2)
            Customer customer = getCustomerFromPrincipal(principal);
            customer = customerRepository.findById(customer.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // Check if customer has password (for OAuth2 users)
            if (customer.getPasswordHash() == null || customer.getPasswordHash().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false,
                                "message", "Tài khoản đăng nhập bằng " + customer.getProvider() +
                                        " không thể đổi mật khẩu"));
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, customer.getPasswordHash())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu hiện tại không đúng"));
            }

            // Update password
            customer.setPasswordHash(passwordEncoder.encode(newPassword));
            customer.setHasCustomPassword(true);
            customerRepository.save(customer);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đổi mật khẩu thành công"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * ============================================
     * API - GET PROFILE
     * ============================================
     */
    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Object principal) {
        try {
            Customer customer = getCustomerFromPrincipal(principal);
            CustomerDTO customerDTO = customerService.getCustomerById(customer.getCustomerId());
            return ResponseEntity.ok(customerDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ============================================
     * API - GET LOYALTY POINTS
     * ============================================
     */
    @GetMapping("/api/loyalty-points")
    @ResponseBody
    public ResponseEntity<?> getLoyaltyPoints(@AuthenticationPrincipal Object principal) {
        try {
            Customer customer = getCustomerFromPrincipal(principal);
            customer = customerRepository.findById(customer.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            return ResponseEntity.ok(Map.of(
                    "loyaltyPoints", customer.getLoyaltyPoints(),
                    "customerTier", customer.getCustomerTier().name(),
                    "totalSpent", customer.getTotalSpent(),
                    "totalOrders", customer.getTotalOrders()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ============================================
     * API - CHECK USERNAME
     * ============================================
     */
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = customerService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * ============================================
     * API - CHECK EMAIL
     * ============================================
     */
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * ============================================
     * API - CHECK PHONE
     * ============================================
     */
    @GetMapping("/check-phone")
    @ResponseBody
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        boolean exists = customerService.existsByPhone(phone);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}