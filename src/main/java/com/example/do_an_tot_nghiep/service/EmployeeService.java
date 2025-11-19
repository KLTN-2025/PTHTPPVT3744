package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.*;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.IEmployeeRepository;
import com.example.do_an_tot_nghiep.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class EmployeeService implements IEmployeeService {
    @Autowired
    private IEmployeeRepository employeeRepository;
    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeDTO registerEmployee(EmployeeRegistrationRequest request) {
        // Validate username and email uniqueness
        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(role)
                .dateOfBirth(request.getDateOfBirth())
                .gender(Employee.Gender.valueOf(request.getGender()))
                .position(request.getPosition())
                .department(request.getDepartment())
                .hireDate(request.getHireDate())
                .salary(request.getSalary())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        employee = employeeRepository.save(employee);

        return convertToDTO(employee);
    }

    @Override
    public EmployeeDTO getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElse(null);
        return employee != null ? convertToDTO(employee) : null;
    }

    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findByStatus(Employee.EmployeeStatus.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> searchEmployees(String keyword) {
        return employeeRepository.searchEmployees(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .username(employee.getUsername())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .avatarUrl(employee.getAvatarUrl())
                .roleId(employee.getRole().getRoleId())
                .roleName(employee.getRole().getRoleName())
                .citizenId(employee.getCitizenId())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender() != null ? employee.getGender().name() : null)
                .position(employee.getPosition())
                .department(employee.getDepartment())
                .hireDate(employee.getHireDate())
                .salary(employee.getSalary())
                .status(employee.getStatus().name())
                .build();
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getActiveCount() {
        return employeeRepository.countByStatus(Employee.EmployeeStatus.ACTIVE);
    }

    @Override
    public long getOnLeaveCount() {
        return employeeRepository.countByStatus(Employee.EmployeeStatus.ON_LEAVE);
    }

    @Override
    public long getTotalEmployees() {
        return employeeRepository.count();
    }

    @Override
    public long getNewEmployeesThisMonth() {
        return employeeRepository.countNewEmployeesThisMonth();
    }

    @Override
    public Page<EmployeeDTO> getFilteredEmployees(String search, String status,
                                                  Integer roleId, String department,
                                                  Pageable pageable) {
        Specification<Employee> spec = null;

        // Search by name, email, employee code, or username
        if (search != null && !search.trim().isEmpty()) {
            Specification<Employee> searchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("fullName")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("employeeCode")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("username")), "%" + search.toLowerCase() + "%")
            );
            spec = spec == null ? searchSpec : spec.and(searchSpec);
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            try {
                Employee.EmployeeStatus employeeStatus = convertToEmployeeStatus(status);
                Specification<Employee> statusSpec = (root, query, cb) ->
                        cb.equal(root.get("status"), employeeStatus);
                spec = spec == null ? statusSpec : spec.and(statusSpec);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        // Filter by role
        if (roleId != null) {
            Specification<Employee> roleSpec = (root, query, cb) ->
                    cb.equal(root.get("role").get("roleId"), roleId);
            spec = spec == null ? roleSpec : spec.and(roleSpec);
        }

        // Filter by department
        if (department != null && !department.isEmpty()) {
            Specification<Employee> deptSpec = (root, query, cb) ->
                    cb.equal(root.get("department"), department);
            spec = spec == null ? deptSpec : spec.and(deptSpec);
        }

        Page<Employee> employees = spec != null
                ? employeeRepository.findAll(spec, pageable)
                : employeeRepository.findAll(pageable);
        return employees.map(this::convertToDTO);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertRoleToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllDepartments() {
        return employeeRepository.findDistinctDepartments();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return employeeRepository.findByPhone(phone).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByUsername(String username) {
        return employeeRepository.findByUsername(username).isPresent();
    }

    @Override
    public Employee findByEmployeeId(Integer employeeId) {
        return employeeRepository.findById(employeeId).orElse(null);
    }

    @Transactional
    @Override
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        // Kiểm tra có thể xóa không
        if (!canDeleteEmployee(id)) {
            throw new RuntimeException(getCannotDeleteReason(id));
        }

        employeeRepository.deleteById(id);
    }

    @Transactional
    @Override
    public int deleteMultiple(List<Integer> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        List<String> errors = new ArrayList<>();

        for (Integer id : employeeIds) {
            try {
                if (canDeleteEmployee(id)) {
                    employeeRepository.deleteById(id);
                    deletedCount++;
                } else {
                    errors.add("ID " + id + ": " + getCannotDeleteReason(id));
                }
            } catch (Exception e) {
                errors.add("ID " + id + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && deletedCount == 0) {
            throw new RuntimeException("Không thể xóa nhân viên: " + String.join("; ", errors));
        }

        return deletedCount;
    }

    @Transactional
    @Override
    public void updateEmployeeStatus(Integer id, String newStatus) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        try {
            Employee.EmployeeStatus status = convertToEmployeeStatus(newStatus);
            employee.setStatus(status);
            employeeRepository.save(employee);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatus);
        }
    }

    @Override
    public boolean canDeleteEmployee(Integer id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (!employeeOpt.isPresent()) {
            return false;
        }

        Employee employee = employeeOpt.get();

        // Không cho xóa admin cuối cùng
        if ("ADMIN".equalsIgnoreCase(employee.getRole().getRoleName())) {
            long adminCount = employeeRepository.countByRole_RoleName("ADMIN");
            if (adminCount <= 1) {
                return false;
            }
        }

        // Kiểm tra xem nhân viên có dữ liệu liên quan không
        // TODO: Thêm các kiểm tra khác như:
        // - Có đơn hàng đang xử lý
        // - Có yêu cầu nghỉ phép chưa xử lý
        // - Là người quản lý của nhân viên khác
        // - Có dự án đang thực hiện

        return true;
    }

    @Override
    public String getCannotDeleteReason(Integer id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (!employeeOpt.isPresent()) {
            return "Nhân viên không tồn tại";
        }

        Employee employee = employeeOpt.get();

        // Kiểm tra admin cuối cùng
        if ("ADMIN".equalsIgnoreCase(employee.getRole().getRoleName())) {
            long adminCount = employeeRepository.countByRole_RoleName("ADMIN");
            if (adminCount <= 1) {
                return "Không thể xóa admin cuối cùng trong hệ thống";
            }
        }

        // TODO: Thêm các lý do khác
        // if (hasActiveOrders) return "Nhân viên có đơn hàng đang xử lý";
        // if (hasPendingLeaveRequests) return "Nhân viên có yêu cầu nghỉ phép chưa xử lý";
        // if (isManagerOfOthers) return "Nhân viên đang quản lý nhân viên khác";

        return "";
    }

    @Override
    public List<Integer> getCannotDeleteEmployeeIds(List<Integer> ids) {
        List<Integer> cannotDelete = new ArrayList<>();
        for (Integer id : ids) {
            if (!canDeleteEmployee(id)) {
                cannotDelete.add(id);
            }
        }
        return cannotDelete;
    }

    @Transactional
    @Override
    public void createEmployee(EmployeeDTO dto) {
        // Validate
        if (employeeRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        Employee employee = Employee.builder()
                .employeeCode(dto.getEmployeeCode())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode("123456")) // Default password
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .role(role)
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender() != null ? Employee.Gender.valueOf(dto.getGender()) : null)
                .position(dto.getPosition())
                .department(dto.getDepartment())
                .hireDate(dto.getHireDate() != null ? dto.getHireDate() : LocalDate.now())
                .salary(dto.getSalary())
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        employeeRepository.save(employee);
    }

    @Transactional
    @Override
    public void updateEmployee(EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        // Validate username và email (trừ chính nhân viên đang update)
        Optional<Employee> existingUsername = employeeRepository.findByUsername(dto.getUsername());
        if (existingUsername.isPresent() && !existingUsername.get().getEmployeeId().equals(dto.getEmployeeId())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        Optional<Employee> existingEmail = employeeRepository.findByEmail(dto.getEmail());
        if (existingEmail.isPresent() && !existingEmail.get().getEmployeeId().equals(dto.getEmployeeId())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        // Update fields
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setUsername(dto.getUsername());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setRole(role);
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender() != null ? Employee.Gender.valueOf(dto.getGender()) : null);
        employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartment());
        employee.setHireDate(dto.getHireDate());
        employee.setSalary(dto.getSalary());

        if (dto.getStatus() != null) {
            employee.setStatus(convertToEmployeeStatus(dto.getStatus()));
        }

        employeeRepository.save(employee);
    }

    /**
     * Convert string status to EmployeeStatus enum
     */
    private Employee.EmployeeStatus convertToEmployeeStatus(String status) {
        if (status == null) {
            return Employee.EmployeeStatus.ACTIVE;
        }

        // Handle both "Active" and "ACTIVE" format
        String normalizedStatus = status.toUpperCase().replace(" ", "_");

        try {
            return Employee.EmployeeStatus.valueOf(normalizedStatus);
        } catch (IllegalArgumentException e) {
            // Try mapping common variants
            switch (status.toLowerCase()) {
                case "active":
                case "đang làm việc":
                    return Employee.EmployeeStatus.ACTIVE;
                case "on leave":
                case "nghỉ phép":
                    return Employee.EmployeeStatus.ON_LEAVE;
                case "resigned":
                case "đã nghỉ việc":
                    return Employee.EmployeeStatus.RESIGNED;
                default:
                    throw new IllegalArgumentException("Invalid status: " + status);
            }
        }
    }

    private RoleDTO convertRoleToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}