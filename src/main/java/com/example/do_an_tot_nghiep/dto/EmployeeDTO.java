package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeeDTO {
    private Integer employeeId;
    private String employeeCode;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String roleName;
    private LocalDate dateOfBirth;
    private String gender;
    private String position;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String status;
}
