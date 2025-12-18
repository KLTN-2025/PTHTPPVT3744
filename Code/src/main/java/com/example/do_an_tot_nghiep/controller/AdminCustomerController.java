package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;


    // Danh sách khách hàng
    @GetMapping
    public String listCustomers(Model model,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String tier,
                                @RequestParam(required = false) String status,
                                @RequestParam(defaultValue = "1") int page) {

        int pageSize = 10;
        Page<Customer> customerPage = customerService.findCustomers(keyword, tier, status, page, pageSize);

        model.addAttribute("customers", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("tiers", Customer.CustomerTier.values());
        model.addAttribute("statuses", Customer.CustomerStatus.values());
        model.addAttribute("selectedTier", tier);
        model.addAttribute("selectedStatus", status);

        return "customer/customer-list";
    }
    @GetMapping("/add")
    public String showAddCustomerForm(Model model) {
        Customer customer = new Customer();
        customer.setProvider("LOCAL");
        customer.setHasCustomPassword(true);
        customer.setCustomerTier(Customer.CustomerTier.BRONZE);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setEmailVerified(false);
        customer.setPhoneVerified(false);
        customer.setLoyaltyPoints(0);

        model.addAttribute("customer", customer);
        model.addAttribute("customerTiers", Customer.CustomerTier.values());
        return "customer/customer-add";
    }

    /**
     * Xử lý thêm khách hàng mới
     */
    @PostMapping("/add")
    public String addCustomer(
            @ModelAttribute Customer customer,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate
            if (customerService.existsByUsername(customer.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
                return "redirect:/admin/customers/add";
            }

            if (customerService.existsByEmail(customer.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng!");
                return "redirect:/admin/customers/add";
            }

            if (customerService.existsByPhone(customer.getPhone())) {
                redirectAttributes.addFlashAttribute("error", "Số điện thoại đã được sử dụng!");
                return "redirect:/admin/customers/add";
            }

            // Set password
            if (password != null && !password.trim().isEmpty()) {
                customer.setPasswordHash(passwordEncoder.encode(password));
                customer.setHasCustomPassword(true);
            } else {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu!");
                return "redirect:/admin/customers/add";
            }

            // Generate codes if empty
            if (customer.getCustomerCode() == null || customer.getCustomerCode().isEmpty()) {
                customer.setCustomerCode(customerService.generateCustomerCode());
            }
            if (customer.getReferralCode() == null || customer.getReferralCode().isEmpty()) {
                customer.setReferralCode(customerService.generateReferralCode());
            }

            // Set default values
            if (customer.getProvider() == null) {
                customer.setProvider("LOCAL");
            }

            // Convert to DTO and save
            CustomerDTO dto = customerService.convertToDTO(customer);
            dto.setPasswordHash(customer.getPasswordHash());
            customerService.save(dto);

            redirectAttributes.addFlashAttribute("success", "Thêm khách hàng thành công!");
            return "redirect:/admin/customers";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/customers/add";
        }
    }

    /**
     * Hiển thị form chỉnh sửa khách hàng
     */
    @GetMapping("/edit/{id}")
    public String showEditCustomerForm(@PathVariable Integer id, Model model) {
        try {
            CustomerDTO customerDTO = customerService.getCustomerById(id);

            // Convert DTO to Entity for form binding
            Customer customer = new Customer();
            customer.setCustomerId(customerDTO.getCustomerId());
            customer.setCustomerCode(customerDTO.getCustomerCode());
            customer.setUsername(customerDTO.getUsername());
            customer.setFullName(customerDTO.getFullName());
            customer.setEmail(customerDTO.getEmail());
            customer.setPhone(customerDTO.getPhone());
            customer.setAddress(customerDTO.getAddress());
            customer.setAvatarUrl(customerDTO.getAvatarUrl());
            customer.setDateOfBirth(customerDTO.getDateOfBirth());
            customer.setGender(customerDTO.getGender() != null ?
                    Customer.Gender.valueOf(customerDTO.getGender()) : null);
            customer.setCustomerTier(Customer.CustomerTier.valueOf(customerDTO.getCustomerTier()));
            customer.setLoyaltyPoints(customerDTO.getLoyaltyPoints());
            customer.setTotalSpent(customerDTO.getTotalSpent());
            customer.setTotalOrders(customerDTO.getTotalOrders());
            customer.setStatus(Customer.CustomerStatus.valueOf(customerDTO.getStatus()));
            customer.setEmailVerified(customerDTO.getEmailVerified());
            customer.setPhoneVerified(customerDTO.getPhoneVerified());
            customer.setProvider(customerDTO.getProvider());
            customer.setProviderId(customerDTO.getProviderId());
            customer.setReferralCode(customerDTO.getReferralCode());
            customer.setLastLogin(customerDTO.getLastLogin());
            customer.setCreatedAt(customerDTO.getCreatedAt());

            model.addAttribute("customer", customer);
            model.addAttribute("customerTiers", Customer.CustomerTier.values());
            return "customer/customer-edit";

        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy khách hàng: " + e.getMessage());
            return "redirect:/admin/customers";
        }
    }

    /**
     * Xử lý cập nhật khách hàng
     */
    @PostMapping("/edit/{id}")
    public String updateCustomer(
            @PathVariable Integer id,
            @ModelAttribute Customer customer,
            @RequestParam(required = false) String newPassword,
            RedirectAttributes redirectAttributes) {

        try {
            CustomerDTO existingCustomer = customerService.getCustomerById(id);

            // Update fields
            existingCustomer.setFullName(customer.getFullName());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setPhone(customer.getPhone());
            existingCustomer.setAddress(customer.getAddress());
            existingCustomer.setDateOfBirth(customer.getDateOfBirth());
            existingCustomer.setGender(customer.getGender() != null ? customer.getGender().name() : null);
            existingCustomer.setCustomerTier(customer.getCustomerTier().name());
            existingCustomer.setLoyaltyPoints(customer.getLoyaltyPoints());
            existingCustomer.setStatus(customer.getStatus().name());
            existingCustomer.setEmailVerified(customer.getEmailVerified());
            existingCustomer.setPhoneVerified(customer.getPhoneVerified());

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                existingCustomer.setPasswordHash(passwordEncoder.encode(newPassword));
                existingCustomer.setHasCustomPassword(true);
            }

            customerService.save(existingCustomer);

            redirectAttributes.addFlashAttribute("success", "Cập nhật khách hàng thành công!");
            return "redirect:/admin/customers";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/customers/edit/" + id;
        }
    }

    /**
     * Xóa một khách hàng
     */
    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomerById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    /**
     * Xóa một khách hàng (DELETE method cho AJAX)
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCustomerAjax(@PathVariable Integer id) {
        try {
            customerService.deleteCustomerById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa khách hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Xóa nhiều khách hàng
     */
    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<?> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            customerService.deleteCustomers(ids);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", ids.size());
            response.put("message", "Đã xóa " + ids.size() + " khách hàng");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API: Lấy thông tin khách hàng theo ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getCustomerApi(@PathVariable Integer id) {
        try {
            CustomerDTO customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Kiểm tra username đã tồn tại chưa
     */
    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = customerService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * API: Kiểm tra email đã tồn tại chưa
     */
    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * API: Kiểm tra phone đã tồn tại chưa
     */
    @GetMapping("/check-phone")
    @ResponseBody
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        boolean exists = customerService.existsByPhone(phone);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}