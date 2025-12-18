package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.ContactMessage;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.IContactMessageService;
import com.example.do_an_tot_nghiep.service.IEmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/contact-messages")
public class AdminContactController {

    @Autowired
    private IContactMessageService contactMessageService;

    @Autowired
    private IEmployeeService employeeService;

    /**
     * Lấy thông tin Employee hiện tại từ SecurityContext
     */
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeDetails) {
            EmployeeDetails employeeDetails = (EmployeeDetails) auth.getPrincipal();
            return employeeDetails.getEmployee();
        }
        return null;
    }

    /**
     * Hiển thị danh sách tất cả tin nhắn
     */
    @GetMapping
    public String listAllMessages(
            @RequestParam(required = false) String status,
            Model model
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        List<ContactMessage> messages;

        if (status != null && !status.isEmpty()) {
            messages = contactMessageService.findByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            messages = contactMessageService.findAll();
        }

        long newCount = contactMessageService.countNewMessages();

        // Đếm số lượng theo từng trạng thái
        long processingCount = messages.stream()
                .filter(m -> "Processing".equals(m.getStatus().name()))
                .count();
        long resolvedCount = messages.stream()
                .filter(m -> "Resolved".equals(m.getStatus().name()))
                .count();

        model.addAttribute("messages", messages);
        model.addAttribute("newCount", newCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("title", "Quản lý tin nhắn liên hệ");
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("isPendingView", false); // ✅ Luôn set giá trị

        return "admin/contact-messages/list";
    }

    /**
     * Xem chi tiết tin nhắn
     */
    @GetMapping("/{id}")
    public String viewMessage(
            @PathVariable("id") Integer messageId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        Optional<ContactMessage> messageOpt = contactMessageService.findById(messageId);

        if (messageOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tin nhắn!");
            return "redirect:/admin/contact-messages";
        }

        ContactMessage message = messageOpt.get();
        List<Employee> employees = employeeService.findAll();

        model.addAttribute("message", message);
        model.addAttribute("employees", employees);
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("title", "Chi tiết tin nhắn #" + messageId);

        return "admin/contact-messages/detail";
    }

    /**
     * Cập nhật trạng thái tin nhắn
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(
            @PathVariable("id") Integer messageId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            contactMessageService.updateStatus(messageId, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/contact-messages/" + messageId;
    }

    /**
     * Gán tin nhắn cho nhân viên
     */
    @PostMapping("/{id}/assign")
    public String assignToEmployee(
            @PathVariable("id") Integer messageId,
            @RequestParam("employeeId") Integer employeeId,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            contactMessageService.assignToEmployee(messageId, employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Gán nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/contact-messages/" + messageId;
    }

    /**
     * Trả lời tin nhắn
     */
    @PostMapping("/{id}/reply")
    public String replyMessage(
            @PathVariable("id") Integer messageId,
            @RequestParam("replyContent") String replyContent,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            if (replyContent == null || replyContent.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung phản hồi không được để trống!");
                return "redirect:/admin/contact-messages/" + messageId;
            }

            contactMessageService.replyMessage(messageId, currentEmployee.getEmployeeId(), replyContent);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi phản hồi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/contact-messages/" + messageId;
    }

    /**
     * Xóa tin nhắn
     */
    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable("id") Integer messageId,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            contactMessageService.deleteMessage(messageId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tin nhắn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/contact-messages";
    }

    /**
     * Xem tin nhắn chưa xử lý
     */
    @GetMapping("/pending")
    public String listPendingMessages(Model model) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        List<ContactMessage> messages = contactMessageService.findPendingMessages();
        long newCount = contactMessageService.countNewMessages();

        // Đếm số lượng theo từng trạng thái
        long processingCount = messages.stream()
                .filter(m -> "Processing".equals(m.getStatus().name()))
                .count();
        long resolvedCount = 0; // Không có resolved trong pending

        model.addAttribute("messages", messages);
        model.addAttribute("newCount", newCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("title", "Tin nhắn chưa xử lý");
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("isPendingView", true);

        return "admin/contact-messages/list";
    }
}