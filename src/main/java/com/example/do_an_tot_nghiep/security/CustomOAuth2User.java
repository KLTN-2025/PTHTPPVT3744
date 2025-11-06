package com.example.do_an_tot_nghiep.security;

import com.example.do_an_tot_nghiep.dto.CustomerDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class CustomOAuth2User implements OAuth2User, UserDetails {

    private CustomerDTO customer;
    private Map<String, Object> attributes;

    public CustomOAuth2User(CustomerDTO customer, Map<String, Object> attributes) {
        this.customer = customer;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Mặc định là CUSTOMER
        authorities.add(new SimpleGrantedAuthority("CUSTOMER"));

        // Nếu email là admin (hoặc có field role trong Customer)
        if (customer.getEmail() != null &&
                (customer.getEmail().endsWith("@admin.com") ||
                        customer.getEmail().equals("admin@example.com"))) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        return authorities;
    }

    @Override
    public String getName() {
        return customer.getEmail();
    }

    @Override
    public String getPassword() {
        return customer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"BLOCKED".equals(customer.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(customer.getStatus());
    }

    // ✅ Getter để lấy customer info
    public CustomerDTO getCustomer() {
        return customer;
    }

    public Integer getCustomerId() {
        return customer.getCustomerId();
    }

    public String getEmail() {
        return customer.getEmail();
    }

    public String getFullName() {
        return customer.getFullName();
    }

    public String getAvatarUrl() {
        return customer.getAvatarUrl();
    }

    public Boolean getHasCustomPassword() {
        return customer.getHasCustomPassword();
    }

    public String getProvider() {
        return customer.getProvider();
    }
}