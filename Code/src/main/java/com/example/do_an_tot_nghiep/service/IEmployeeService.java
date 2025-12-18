package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.EmployeeDTO;
import com.example.do_an_tot_nghiep.dto.RoleDTO;
import com.example.do_an_tot_nghiep.model.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmployeeService {

    // Basic CRUD operations
    EmployeeDTO getEmployeeById(Integer employeeId);
    List<EmployeeDTO> getAllEmployees();
    List<Employee> findAll(); // Thêm method này
    void createEmployee(EmployeeDTO dto);
    void updateEmployee(EmployeeDTO dto);
    void deleteEmployee(Integer id);

    // Statistics
    long getActiveCount();
    long getOnLeaveCount();
    long getTotalEmployees();
    long getNewEmployeesThisMonth();

    // Search and Filter
    Page<EmployeeDTO> getFilteredEmployees(String search, String status,
                                           Integer roleId, String department,
                                           Pageable pageable);

    // Batch operations
    int deleteMultiple(List<Integer> employeeIds);
    List<Integer> getCannotDeleteEmployeeIds(List<Integer> ids);

    // Status management
    void updateEmployeeStatus(Integer id, String newStatus);

    // Validation
    boolean canDeleteEmployee(Integer id);
    String getCannotDeleteReason(Integer id);

    // Reference data
    List<RoleDTO> getAllRoles();
    List<String> getAllDepartments();

    // Check existence
    boolean existsByPhone(@NotBlank(message = "Số điện thoại không được để trống")
                          @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 số")
                          String phone);

    boolean existsByEmail(@NotBlank(message = "Email không được để trống")
                          @Email(message = "Email không hợp lệ")
                          String email);

    boolean existsByUsername(@NotBlank(message = "Tên đăng nhập không được để trống")
                             @Size(min = 4, max = 100, message = "Tên đăng nhập phải từ 4-100 ký tự")
                             String username);

    // Find by ID
    Employee findByEmployeeId(Integer employeeId);
}