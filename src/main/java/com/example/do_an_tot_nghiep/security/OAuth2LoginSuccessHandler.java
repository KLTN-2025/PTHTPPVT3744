package com.example.do_an_tot_nghiep.security;

import com.example.do_an_tot_nghiep.model.Customer;
import com.example.do_an_tot_nghiep.repository.ICustomerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ICustomerRepository customerRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Customer customer = customerRepository.findByEmail(email).orElse(null);

        if (customer != null && Boolean.FALSE.equals(customer.getHasCustomPassword())) {
            // ⚠️ Chưa có mật khẩu → chuyển tới trang đặt mật khẩu
            response.sendRedirect("/auth/set-password");
        } else {
            // ✅ Đã có mật khẩu → chuyển hướng như bình thường
            response.sendRedirect("/redirectByRole");
        }
    }
}
