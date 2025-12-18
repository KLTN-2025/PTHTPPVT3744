package com.example.do_an_tot_nghiep.config;

import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.example.do_an_tot_nghiep.controller")
@RequiredArgsConstructor
public class AdminGlobalModelAttributes {

    private final NotificationService notificationService;

    @ModelAttribute("unreadNotifications")
    public Long addUnreadNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof EmployeeDetails employeeDetails) {
            return notificationService.getUnreadCountByEmployee(employeeDetails.getEmployeeId());
        }

        return 0L;
    }
}