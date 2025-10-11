package com.example.do_an_tot_nghiep.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userType;
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
}
