package com.example.do_an_tot_nghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer employeeId;
    private String employeeCode;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;

    // Role information
    private Integer roleId;        // ID của role (dùng cho form select)
    private String roleName;       // Tên role (dùng để hiển thị)
    private String citizenId;
    private LocalDate dateOfBirth;
    private String gender;         // MALE, FEMALE, OTHER
    private String position;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String status;         // ACTIVE, ON_LEAVE, RESIGNED

    // Display helpers
    public String getStatusDisplay() {
        if (status == null) return "N/A";
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "Đang làm việc";
            case "ON_LEAVE":
                return "Nghỉ phép";
            case "RESIGNED":
                return "Đã nghỉ việc";
            default:
                return status;
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "badge-success";
            case "ON_LEAVE":
                return "badge-warning";
            case "RESIGNED":
                return "badge-secondary";
            default:
                return "badge-secondary";
        }
    }

    public String getGenderDisplay() {
        if (gender == null) return "N/A";
        switch (gender.toUpperCase()) {
            case "MALE":
                return "Nam";
            case "FEMALE":
                return "Nữ";
            case "OTHER":
                return "Khác";
            default:
                return gender;
        }
    }
}