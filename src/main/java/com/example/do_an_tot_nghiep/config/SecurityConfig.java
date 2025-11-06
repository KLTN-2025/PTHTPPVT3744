package com.example.do_an_tot_nghiep.config;

import com.example.do_an_tot_nghiep.security.OAuth2LoginSuccessHandler;
import com.example.do_an_tot_nghiep.service.CustomOAuth2UserService;
import com.example.do_an_tot_nghiep.service.MultiUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MultiUserDetailsService multiUserDetailsService;
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(multiUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Static resources - PUBLIC
                        .requestMatchers("/auth/**", "/register", "/css/**", "/js/**", "/images/**", "/img/**", "/favicon.ico").permitAll()

                        // Frontend pages - PUBLIC (không cần đăng nhập)
                        .requestMatchers("/", "/home", "/products", "/products/**", "/cart", "/cart/**").permitAll()
                        .requestMatchers("/promotions", "/promotions/**", "/support").permitAll()

                        // Contact - PUBLIC (ai cũng gửi được tin nhắn)
                        .requestMatchers("/contact", "/contact/", "/contact/submit").permitAll()

                        // My Messages - AUTHENTICATED (chỉ user đã đăng nhập)
                        // ✅ FIX: Thêm authenticated() để đảm bảo user đã login
                        .requestMatchers("/contact/my-messages", "/contact/message/**").authenticated()

                        // Admin area - ADMIN/MANAGER/STAFF only
                        .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF", "ROLE_WAREHOUSE")

                        // User area - CUSTOMER only
                        .requestMatchers("/user/**").hasAuthority("ROLE_CUSTOMER")

                        // Các request còn lại cần authenticate
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .failureUrl("/auth/login?error")
                        .defaultSuccessUrl("/redirectByRole", true)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // ✅ FIX: Thêm exceptionHandling để redirect đúng khi access denied
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/auth/login?error=access-denied")
                );

        return http.build();
    }
}