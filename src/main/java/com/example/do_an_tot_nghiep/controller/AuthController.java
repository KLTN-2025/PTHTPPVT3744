package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.CustomerRegistrationRequest;
import com.example.do_an_tot_nghiep.service.CustomerService;
import com.example.do_an_tot_nghiep.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("loginError", "Tên tài khoản hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công!");
        }

        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }

        return "auth/login";
    }

    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER") || role.equals("ROLE_STAFF")) {
            return "redirect:/admin/dashboard";
        } else if (role.equals("ROLE_CUSTOMER")) {
            return "redirect:/home";
        }

        return "redirect:/auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("customer", new CustomerRegistrationRequest());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerCustomer(
            @Valid @ModelAttribute("customer") CustomerRegistrationRequest customerDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Kiểm tra validation errors
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            // Kiểm tra username đã tồn tại trong Customer hoặc Employee
            if (customerService.existsByUsername(customerDto.getUsername()) ||
                    employeeService.existsByUsername(customerDto.getUsername())) {
                model.addAttribute("errorMessage", "Tên đăng nhập đã tồn tại!");
                return "auth/register";
            }

            // Kiểm tra email đã tồn tại trong Customer hoặc Employee
            if (customerService.existsByEmail(customerDto.getEmail()) ||
                    employeeService.existsByEmail(customerDto.getEmail())) {
                model.addAttribute("errorMessage", "Email đã được sử dụng!");
                return "auth/register";
            }

            // Kiểm tra phone đã tồn tại trong Customer hoặc Employee
            if (customerService.existsByPhone(customerDto.getPhone()) ||
                    employeeService.existsByPhone(customerDto.getPhone())) {
                model.addAttribute("errorMessage", "Số điện thoại đã được đăng ký!");
                return "auth/register";
            }

            // Kiểm tra mật khẩu khớp
            if (!customerDto.getPassword().equals(customerDto.getConfirmPassword())) {
                model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "auth/register";
            }

            // Đăng ký customer
            customerService.registerCustomer(customerDto);

            // Redirect về trang login với thông báo thành công
            redirectAttributes.addAttribute("registered", "true");
            return "redirect:/auth/login";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đăng ký không thành công. Vui lòng thử lại!");
            return "auth/register";
        }
    }


    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }
}