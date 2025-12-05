package com.example.do_an_tot_nghiep.dto;

import com.example.do_an_tot_nghiep.model.Supplier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Integer supplierId;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 255, message = "Tên người liên hệ không được vượt quá 255 ký tự")
    private String contactPerson;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 50, message = "Mã số thuế không được vượt quá 50 ký tự")
    private String taxCode;

    @Size(max = 100, message = "Số tài khoản không được vượt quá 100 ký tự")
    private String bankAccount;

    @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự")
    private String bankName;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Convert từ Entity sang DTO
    public static SupplierDTO fromEntity(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setAddress(supplier.getAddress());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setTaxCode(supplier.getTaxCode());
        dto.setBankAccount(supplier.getBankAccount());
        dto.setBankName(supplier.getBankName());
        dto.setDescription(supplier.getDescription());
        dto.setStatus(supplier.getStatus() != null ? supplier.getStatus().name() : null);
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        return dto;
    }

    // Convert từ DTO sang Entity (sử dụng Builder pattern)
    public Supplier toEntity() {
        return Supplier.builder()
                .supplierId(this.supplierId)
                .name(this.name)
                .contactPerson(this.contactPerson)
                .address(this.address)
                .phone(this.phone)
                .email(this.email)
                .taxCode(this.taxCode)
                .bankAccount(this.bankAccount)
                .bankName(this.bankName)
                .description(this.description)
                .status(this.status != null ? Supplier.SupplierStatus.valueOf(this.status) : Supplier.SupplierStatus.ACTIVE)
                .build();
    }
}