package com.example.do_an_tot_nghiep.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("statusCode", statusCode);

            // Trả về template trong folder error/
            switch (statusCode) {
                case 400: return "error/400";
                case 403: return "error/403";
                case 404: return "error/404";
                case 500: return "error/500";
                case 503: return "error/503";
                default: return "error/default";
            }
        }
        return "error/default";
    }
}
