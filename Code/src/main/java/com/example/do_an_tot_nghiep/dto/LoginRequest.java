package com.example.do_an_tot_nghiep.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
    private String userType; // "CUSTOMER" or "EMPLOYEE"
}
