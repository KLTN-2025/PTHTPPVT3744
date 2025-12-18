package com.example.do_an_tot_nghiep.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.do_an_tot_nghiep.dto.EmployeeDTO;
import com.example.do_an_tot_nghiep.dto.RoleDTO;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Role;
import com.example.do_an_tot_nghiep.repository.*;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private IEmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private IRoleRepository roleRepository;
    /**
     * Hiển thị danh sách nhân viên với filter và pagination
     */
    @GetMapping
    public String manageEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal EmployeeDetails currentEmployee,
            Model model) {

        // Get filtered employees with pagination
        Page<EmployeeDTO> employeePage = employeeService.getFilteredEmployees(
                search, status, roleId, department, PageRequest.of(page, size));

        // Get employee statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeCount", employeeService.getActiveCount());
        stats.put("onLeaveCount", employeeService.getOnLeaveCount());
        stats.put("totalEmployees", employeeService.getTotalEmployees());
        stats.put("newEmployeesThisMonth", employeeService.getNewEmployeesThisMonth());

        // Get all roles for filter dropdown
        List<RoleDTO> roles = employeeService.getAllRoles();

        // Get all departments for filter dropdown
        List<String> departments = employeeService.getAllDepartments();

        // Add attributes to model
        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("employeeStats", stats);
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("roles", roles);
        model.addAttribute("departments", departments);

        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("totalItems", employeePage.getTotalElements());

        // Filter parameters (to maintain state)
        model.addAttribute("searchKeyword", search);
        model.addAttribute("status", status);
        model.addAttribute("roleId", roleId);
        model.addAttribute("department", department);
        return "employee/employee-list";
    }

    /**
     * Hiển thị form thêm nhân viên mới
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new EmployeeDTO());
        model.addAttribute("roles", employeeService.getAllRoles());
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee/employee-edit";
    }

    /**
     * Hiển thị form chỉnh sửa nhân viên
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,@AuthenticationPrincipal EmployeeDetails currentUser, Model model) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return "redirect:/admin/employees?error=notfound";
        }
        model.addAttribute("employee", employee);
        model.addAttribute("roles", employeeService.getAllRoles());
        model.addAttribute("currentEmployee", currentUser);
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee/employee-edit";
    }

    /**
     * Xóa một nhân viên - FIXED VERSION
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Gọi service để xóa - service sẽ tự validate
            employeeService.deleteEmployee(id);

            response.put("success", true);
            response.put("message", "Xóa nhân viên thành công");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());

            // Phân loại lỗi dựa vào message
            String errorMsg = e.getMessage().toLowerCase();

            if (errorMsg.contains("không tìm thấy") || errorMsg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (errorMsg.contains("không thể xóa") || errorMsg.contains("cannot delete") ||
                    errorMsg.contains("admin cuối cùng")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống khi xóa nhân viên: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa nhiều nhân viên cùng lúc - FIXED VERSION
     */
    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMultipleEmployees(@RequestBody List<Integer> employeeIds) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate input
            if (employeeIds == null || employeeIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "Danh sách nhân viên trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Thực hiện xóa - service sẽ tự xử lý validation
            int deletedCount = employeeService.deleteMultiple(employeeIds);

            response.put("success", true);
            response.put("message", "Đã xóa thành công " + deletedCount + " nhân viên");
            response.put("deletedCount", deletedCount);

            // Nếu không xóa được hết, thông báo
            if (deletedCount < employeeIds.size()) {
                response.put("warning", "Một số nhân viên không thể xóa do có ràng buộc dữ liệu");
                response.put("requestedCount", employeeIds.size());
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống khi xóa nhiều nhân viên: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * API kiểm tra nhân viên có thể xóa không
     */
    @GetMapping("/can-delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> canDeleteEmployee(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean canDelete = employeeService.canDeleteEmployee(id);
            String reason = canDelete ? null : employeeService.getCannotDeleteReason(id);

            response.put("canDelete", canDelete);
            response.put("reason", reason);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("canDelete", false);
            response.put("reason", "Lỗi khi kiểm tra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lưu nhân viên (thêm mới hoặc cập nhật)
     */
    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute EmployeeDTO employeeDTO, Model model) {
        try {
            if (employeeDTO.getEmployeeId() == null) {
                // Thêm mới
                employeeService.createEmployee(employeeDTO);
            } else {
                // Cập nhật
                employeeService.updateEmployee(employeeDTO);
            }
            return "redirect:/admin/employees?success=true";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi lưu nhân viên: " + e.getMessage());
            model.addAttribute("employee", employeeDTO);
            model.addAttribute("roles", employeeService.getAllRoles());
            model.addAttribute("departments", employeeService.getAllDepartments());
            return "employee/employee-edit";
        }
    }

    /**
     * API lấy thông tin chi tiết nhân viên
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<EmployeeDTO> getEmployeeDetails(@PathVariable Integer id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    /**
     * API thay đổi trạng thái nhân viên
     */
    @PatchMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateEmployeeStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> statusUpdate) {

        Map<String, Object> response = new HashMap<>();

        try {
            String newStatus = statusUpdate.get("status");
            employeeService.updateEmployeeStatus(id, newStatus);

            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * Xem profile của nhân viên đang đăng nhập
     */
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        // Lấy thông tin user đang đăng nhập từ Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Tìm employee theo username
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));

        // Thêm employee vào model
        model.addAttribute("employee", employee);

        return "employee/employee-profile";
    }

    /**
     * Xem profile của nhân viên khác (dành cho admin/manager)
     */
    @GetMapping("/profile/{id}")
    public String viewEmployeeProfile(@PathVariable Integer id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        model.addAttribute("employee", employee);

        return "employee/employee-profile";
    }

    /**
     * Upload avatar
     */
    @PostMapping("/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestParam(required = false) Integer employeeId) { // nhận employeeId từ frontend

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File không được để trống"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Chỉ chấp nhận file ảnh"));
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kích thước file không được vượt quá 5MB"));
            }

            // Lấy employee cần cập nhật
            Employee employee;
            if (employeeId != null) {
                // Kiểm tra quyền admin
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("success", false, "message", "Bạn không có quyền thay đổi avatar nhân viên khác"));
                }

                // Lấy employee theo ID
                employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
            } else {
                // Nếu không có employeeId → upload cho chính mình
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                employee = employeeRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
            }

            // Upload avatar lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "avatars",
                    "public_id", "emp_" + employee.getEmployeeId() + "_" + UUID.randomUUID(),
                    "overwrite", true,
                    "resource_type", "image"
            ));
            String imageUrl = uploadResult.get("secure_url").toString();

            // Xóa avatar cũ nếu có
            if (employee.getAvatarUrl() != null && !employee.getAvatarUrl().isEmpty()) {
                try {
                    String oldPublicId = employee.getAvatarUrl()
                            .substring(employee.getAvatarUrl().lastIndexOf("/") + 1)
                            .split("\\.")[0];
                    cloudinary.uploader().destroy("avatars/" + oldPublicId, ObjectUtils.emptyMap());
                } catch (Exception ignored) {}
            }

            employee.setAvatarUrl(imageUrl);
            employeeRepository.save(employee);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Cập nhật ảnh đại diện thành công",
                    "avatarUrl", imageUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi upload: " + e.getMessage()));
        }
    }


    /**
     * Cập nhật thông tin profile
     */
    @PostMapping("/update/{id}")
    public String updateProfile(
            @PathVariable("id") Integer id,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String citizenId,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
            @RequestParam(required = false) Double salary,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roleId,
            @AuthenticationPrincipal EmployeeDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            // Cập nhật thông tin cá nhân
            employee.setFullName(fullName);
            employee.setPhone(phone);
            employee.setAddress(address);
            if (dateOfBirth != null) employee.setDateOfBirth(dateOfBirth);
            if (gender != null && !gender.isEmpty()) employee.setGender(Employee.Gender.valueOf(gender));
            if (citizenId != null && !citizenId.isEmpty()) employee.setCitizenId(citizenId);

            // Chỉ admin mới update thông tin công việc
            if (currentUser.hasRole("ADMIN")) {
                if (position != null) employee.setPosition(position);
                if (department != null) employee.setDepartment(department);
                if (hireDate != null) employee.setHireDate(hireDate);
                if (salary != null) employee.setSalary(BigDecimal.valueOf(salary));
                if (status != null && !status.isEmpty()) employee.setStatus(Employee.EmployeeStatus.valueOf(status));

                // Update role từ roleId
                if (roleId != null) {
                    Role role = roleRepository.findById(Math.toIntExact(roleId))
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy role"));
                    employee.setRole(role);
                }
            }

            employeeRepository.save(employee);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }

        return "redirect:/admin/employees/edit/" + id;
    }


    /**
     * Đổi mật khẩu
     */
    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {

        try {
            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu mới không khớp"));
            }

            // Validate password strength
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu phải có ít nhất 6 ký tự"));
            }

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Employee employee = employeeRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, employee.getPasswordHash())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Mật khẩu hiện tại không đúng"));
            }

            // Update password
            employee.setPasswordHash(passwordEncoder.encode(newPassword));
            employeeRepository.save(employee);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đổi mật khẩu thành công"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
}