package com.example.do_an_tot_nghiep.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        // ✅ Kiểm tra parameter "error" (không cần giá trị cụ thể)
        if (error != null) {
            model.addAttribute("loginError", "Tên tài khoản hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công!");
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
}