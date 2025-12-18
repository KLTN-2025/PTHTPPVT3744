package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import com.example.do_an_tot_nghiep.security.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private CustomerService customerService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            System.err.println("❌ OAuth2 Error: " + ex.getMessage());
            ex.printStackTrace();
            throw new OAuth2AuthenticationException("OAuth2 processing failed: " + ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");
        String avatarUrl = oauth2User.getAttribute("picture");
        String providerId = oauth2User.getAttribute("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // GOOGLE

        // Tìm Customer theo email
        CustomerDTO customerDTO = customerService.findByEmail(email);

        if (customerDTO == null) {
            // ✅ Tạo mới OAuth2 customer
            customerDTO = CustomerDTO.builder()
                    .email(email)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .username(generateUsername(email))
                    .provider(provider)
                    .providerId(providerId)
                    .passwordHash(null)  // ✅ OAuth2 không có password
                    .hasCustomPassword(false)
                    .status("ACTIVE")
                    .emailVerified(true)
                    .customerTier("BRONZE")
                    .loyaltyPoints(0)
                    .totalOrders(0)
                    .build();

            customerDTO = customerService.save(customerDTO);
            System.out.println("✅ New Google user created: " + email);

        } else {
            // ✅ Cập nhật thông tin
            customerDTO.setFullName(fullName);
            customerDTO.setAvatarUrl(avatarUrl);
            customerDTO.setEmailVerified(true);

            // Update provider nếu chưa có
            if (customerDTO.getProvider() == null) {
                customerDTO.setProvider(provider);
                customerDTO.setProviderId(providerId);
            }

            customerDTO = customerService.save(customerDTO);
            System.out.println("✅ Updated existing user: " + email);
        }

        // ✅ Trả về CustomOAuth2User với thông tin customer
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("customerId", customerDTO.getCustomerId());
        attributes.put("hasCustomPassword", customerDTO.getHasCustomPassword());

        return new CustomOAuth2User(customerDTO, attributes);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        while (customerService.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}