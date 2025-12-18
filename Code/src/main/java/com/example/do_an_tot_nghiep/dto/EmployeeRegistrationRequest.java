package com.example.do_an_tot_nghiep.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class EmployeeRegistrationRequest {
    private String employeeCode;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Integer roleId;
    private LocalDate dateOfBirth;
    private String gender;
    private String position;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
}
