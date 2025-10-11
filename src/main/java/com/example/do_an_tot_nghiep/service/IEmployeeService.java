package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.EmployeeDTO;
import com.example.do_an_tot_nghiep.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmployeeService {

    // Basic CRUD operations
    EmployeeDTO getEmployeeById(Integer employeeId);
    List<EmployeeDTO> getAllEmployees();
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
}