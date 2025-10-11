package com.example.do_an_tot_nghiep.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CustomerRegistrationRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
}
