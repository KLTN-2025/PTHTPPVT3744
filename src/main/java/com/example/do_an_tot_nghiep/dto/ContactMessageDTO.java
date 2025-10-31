package com.example.do_an_tot_nghiep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDTO {

    private Integer customerId; // Null nếu chưa đăng nhập

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(min = 2, max = 255, message = "Họ tên phải từ 2-255 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải là 10 chữ số")
    private String phone;

    @NotBlank(message = "Vui lòng nhập chủ đề")
    @Size(min = 5, max = 255, message = "Chủ đề phải từ 5-255 ký tự")
    private String subject;

    @NotBlank(message = "Vui lòng nhập nội dung")
    @Size(min = 10, max = 5000, message = "Nội dung phải từ 10-5000 ký tự")
    private String message;
}