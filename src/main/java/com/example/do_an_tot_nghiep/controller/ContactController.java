package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.ContactMessageDTO;
import com.example.do_an_tot_nghiep.model.ContactMessage;
import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.security.CustomOAuth2User;
import com.example.do_an_tot_nghiep.service.IContactMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private IContactMessageService contactMessageService;

    /**
     * Helper method để lấy Customer từ Authentication
     */
    private CustomerInfo getCustomerInfo(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // Trường hợp đăng nhập bằng OAuth2 (Google)
        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User oauth2User = (CustomOAuth2User) principal;
            return new CustomerInfo(
                    oauth2User.getCustomerId(),
                    oauth2User.getFullName(),
                    oauth2User.getEmail(),
                    null // OAuth2 không có phone trong profile
            );
        }

        // Trường hợp đăng nhập thông thường
        if (principal instanceof Customer) {
            Customer customer = (Customer) principal;
            return new CustomerInfo(
                    customer.getCustomerId(),
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhone()
            );
        }

        return null;
    }

    /**
     * Hiển thị trang liên hệ
     */
    @GetMapping({"", "/"})
    public String contactPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomerInfo customerInfo = getCustomerInfo(auth);

        if (customerInfo != null) {
            model.addAttribute("customerName", customerInfo.fullName);
            model.addAttribute("customerEmail", customerInfo.email);
            model.addAttribute("customerPhone", customerInfo.phone);
            model.addAttribute("customerId", customerInfo.customerId);
        }

        model.addAttribute("title", "Liên hệ - Vật Tư Y Tế ABC");
        return "contact/contact";
    }

    /**
     * Xử lý gửi tin nhắn liên hệ
     */
    @PostMapping("/submit")
    public String submitContact(
            @Valid @ModelAttribute ContactMessageDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
                return "redirect:/contact";
            }

            contactMessageService.saveContactMessage(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.");

        } catch (Exception e) {
            System.err.println("Error submitting contact: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra! Vui lòng thử lại sau.");
        }

        return "redirect:/contact";
    }

    /**
     * Hiển thị danh sách tin nhắn của khách hàng
     */
    @GetMapping("/my-messages")
    public String myMessages(Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomerInfo customerInfo = getCustomerInfo(auth);

        if (customerInfo == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vui lòng đăng nhập để xem tin nhắn của bạn!");
            return "redirect:/auth/login";
        }

        // Lấy danh sách tin nhắn của khách hàng
        List<ContactMessage> messages = contactMessageService.findByCustomerId(customerInfo.customerId);

        model.addAttribute("messages", messages);
        model.addAttribute("customerName", customerInfo.fullName);
        model.addAttribute("title", "Tin nhắn của tôi - Vật Tư Y Tế ABC");

        return "contact/my-messages";
    }

    /**
     * Xem chi tiết tin nhắn
     */
    @GetMapping("/message/{messageId}")
    public String viewMessage(@PathVariable Integer messageId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomerInfo customerInfo = getCustomerInfo(auth);

        if (customerInfo == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Vui lòng đăng nhập để xem tin nhắn!");
            return "redirect:/auth/login";
        }

        // Lấy tin nhắn
        Optional<ContactMessage> messageOpt = contactMessageService.findById(messageId);

        if (messageOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không tìm thấy tin nhắn!");
            return "redirect:/contact/my-messages";
        }

        ContactMessage message = messageOpt.get();

        // Kiểm tra quyền truy cập (chỉ chủ tin nhắn mới được xem)
        if (message.getCustomer() == null ||
                !message.getCustomer().getCustomerId().equals(customerInfo.customerId)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bạn không có quyền xem tin nhắn này!");
            return "redirect:/contact/my-messages";
        }

        model.addAttribute("message", message);
        model.addAttribute("title", "Chi tiết tin nhắn - Vật Tư Y Tế ABC");

        return "contact/message-detail";
    }

    /**
     * Inner class để lưu thông tin customer
     */
    private static class CustomerInfo {
        Integer customerId;
        String fullName;
        String email;
        String phone;

        CustomerInfo(Integer customerId, String fullName, String email, String phone) {
            this.customerId = customerId;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
        }
    }
}