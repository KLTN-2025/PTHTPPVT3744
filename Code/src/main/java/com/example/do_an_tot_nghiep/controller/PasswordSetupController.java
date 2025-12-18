package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PasswordSetupController {

    private final ICustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/auth/set-password")
    public String showSetPasswordForm(Model model, Principal principal) {
        model.addAttribute("email", principal.getName());
        return "auth/set_password";
    }

    @PostMapping("/auth/set-password")
    public String setPassword(@RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu không khớp!");
            return "redirect:/auth/set-password";
        }

        Customer customer = customerRepository.findByEmail(email).orElse(null);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
            return "redirect:/auth/set-password";
        }

        // Cập nhật mật khẩu mới
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setHasCustomPassword(true);
        customer.setProvider("LOCAL");

        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mật khẩu thành công!");
        return "redirect:/redirectByRole";
    }
}
