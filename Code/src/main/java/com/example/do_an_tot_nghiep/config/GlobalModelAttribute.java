package com.example.do_an_tot_nghiep.config;

import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttribute {

    private final EmployeeService employeeService;

    @ModelAttribute
    public void addCurrentEmployee(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof EmployeeDetails employeeDetails) {
                Employee employee = employeeService.findByEmployeeId(employeeDetails.getEmployeeId());
                model.addAttribute("currentEmployee", employee);
            }
        }
    }
}
