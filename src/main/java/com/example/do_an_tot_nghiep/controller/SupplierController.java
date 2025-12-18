package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.SupplierDTO;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.security.EmployeeDetails;
import com.example.do_an_tot_nghiep.service.ISupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final ISupplierService supplierService;

    /**
     * Lấy thông tin Employee hiện tại từ SecurityContext
     */
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof EmployeeDetails) {
            EmployeeDetails employeeDetails = (EmployeeDetails) auth.getPrincipal();
            return employeeDetails.getEmployee();
        }
        return null;
    }

    /**
     * Hiển thị danh sách nhà cung cấp
     * GET /admin/suppliers
     */
    @GetMapping
    public String listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "supplierId,desc") String[] sort,
            Model model
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            // Xử lý sorting
            String sortField = sort[0];
            String sortDirection = sort.length > 1 ? sort[1] : "desc";
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            Page<SupplierDTO> suppliersPage;

            // Tìm kiếm hoặc lấy tất cả
            if ((keyword != null && !keyword.trim().isEmpty()) ||
                    (status != null && !status.isEmpty() && !status.equals("ALL"))) {
                suppliersPage = supplierService.searchSuppliersByStatus(keyword, status, pageable);
            } else {
                suppliersPage = supplierService.getAllSuppliers(pageable);
            }

            // Thống kê
            long totalSuppliers = supplierService.getTotalSuppliers();
            long activeSuppliers = supplierService.countByStatus("ACTIVE");
            long inactiveSuppliers = supplierService.countByStatus("INACTIVE");

            model.addAttribute("suppliers", suppliersPage.getContent());
            model.addAttribute("currentPage", suppliersPage.getNumber());
            model.addAttribute("totalItems", suppliersPage.getTotalElements());
            model.addAttribute("totalPages", suppliersPage.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("keyword", keyword);
            model.addAttribute("filterStatus", status);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDirection", sortDirection);

            model.addAttribute("totalSuppliers", totalSuppliers);
            model.addAttribute("activeSuppliers", activeSuppliers);
            model.addAttribute("inactiveSuppliers", inactiveSuppliers);

            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Quản lý nhà cung cấp");

            return "admin/suppliers/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tải danh sách: " + e.getMessage());
            return "admin/suppliers/list";
        }
    }

    /**
     * Hiển thị form tạo mới nhà cung cấp
     * GET /admin/suppliers/new
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("supplier", new SupplierDTO());
        model.addAttribute("currentEmployee", currentEmployee);
        model.addAttribute("title", "Thêm nhà cung cấp mới");
        model.addAttribute("isEdit", false);

        return "admin/suppliers/form";
    }

    /**
     * Xử lý tạo mới nhà cung cấp
     * POST /admin/suppliers
     */
    @PostMapping
    public String createSupplier(
            @Valid @ModelAttribute("supplier") SupplierDTO supplierDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Thêm nhà cung cấp mới");
            model.addAttribute("isEdit", false);
            return "admin/suppliers/form";
        }

        try {
            SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo nhà cung cấp thành công!");
            return "redirect:/admin/suppliers/" + createdSupplier.getSupplierId();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Thêm nhà cung cấp mới");
            model.addAttribute("isEdit", false);
            return "admin/suppliers/form";
        }
    }

    /**
     * Xem chi tiết nhà cung cấp
     * GET /admin/suppliers/{id}
     */
    @GetMapping("/{id}")
    public String viewSupplier(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            // Lấy thông tin supplier
            SupplierDTO supplier = supplierService.getSupplierById(id);

            // ✅ Lấy thống kê
            Map<String, Object> statistics = supplierService.getSupplierStatistics(id);

            // ✅ Thêm supplier vào model TRƯỚC
            model.addAttribute("supplier", supplier);

            // ✅ Truyền dữ liệu thống kê vào model
            model.addAttribute("productCount", statistics.get("productCount"));
            model.addAttribute("importCount", statistics.get("importCount"));
            model.addAttribute("totalValue", statistics.get("totalValue"));

            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Chi tiết nhà cung cấp: " + supplier.getName());

            return "admin/suppliers/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/suppliers";
        }
    }

    /**
     * Hiển thị form chỉnh sửa
     * GET /admin/suppliers/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Integer id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            SupplierDTO supplier = supplierService.getSupplierById(id);

            model.addAttribute("supplier", supplier);
            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Chỉnh sửa nhà cung cấp: " + supplier.getName());
            model.addAttribute("isEdit", true);

            return "admin/suppliers/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/suppliers";
        }
    }

    /**
     * Xử lý cập nhật nhà cung cấp
     * POST /admin/suppliers/{id}/update
     */
    @PostMapping("/{id}/update")
    public String updateSupplier(
            @PathVariable Integer id,
            @Valid @ModelAttribute("supplier") SupplierDTO supplierDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Chỉnh sửa nhà cung cấp");
            model.addAttribute("isEdit", true);
            return "admin/suppliers/form";
        }

        try {
            supplierService.updateSupplier(id, supplierDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật nhà cung cấp thành công!");
            return "redirect:/admin/suppliers/" + id;
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("currentEmployee", currentEmployee);
            model.addAttribute("title", "Chỉnh sửa nhà cung cấp");
            model.addAttribute("isEdit", true);
            return "admin/suppliers/form";
        }
    }

    /**
     * Vô hiệu hóa nhà cung cấp
     * POST /admin/suppliers/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateSupplier(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            supplierService.deactivateSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vô hiệu hóa nhà cung cấp thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/suppliers/" + id;
    }

    /**
     * Kích hoạt nhà cung cấp
     * POST /admin/suppliers/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public String activateSupplier(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            supplierService.activateSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Kích hoạt nhà cung cấp thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/suppliers/" + id;
    }

    /**
     * Xóa nhà cung cấp
     * POST /admin/suppliers/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String deleteSupplier(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        Employee currentEmployee = getCurrentEmployee();
        if (currentEmployee == null) {
            return "redirect:/auth/login";
        }

        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xóa nhà cung cấp thành công!");
            return "redirect:/admin/suppliers";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/suppliers/" + id;
        }
    }
}