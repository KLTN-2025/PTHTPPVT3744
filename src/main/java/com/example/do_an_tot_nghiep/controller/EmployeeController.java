package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.EmployeeDTO;
import com.example.do_an_tot_nghiep.dto.RoleDTO;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

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
        return "employee/employee-form";
    }

    /**
     * Hiển thị form chỉnh sửa nhân viên
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return "redirect:/admin/employees?error=notfound";
        }
        model.addAttribute("employee", employee);
        model.addAttribute("roles", employeeService.getAllRoles());
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee/employee-form";
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
            return "employee/employee-form";
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
}