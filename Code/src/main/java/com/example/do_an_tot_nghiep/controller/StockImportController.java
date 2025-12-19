package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.StockImportDTO;
import com.example.do_an_tot_nghiep.model.StockImport;
import com.example.do_an_tot_nghiep.service.StockImportService;
import com.example.do_an_tot_nghiep.service.SupplierService;
import com.example.do_an_tot_nghiep.service.MedicalDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class StockImportController {

    private final StockImportService stockImportService;
    private final SupplierService supplierService;
    private final MedicalDeviceService medicalDeviceService;

    /**
     * Hiển thị danh sách phiếu nhập kho
     */
    @GetMapping
    public String listStockImports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        // Convert LocalDate to LocalDateTime
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        // Sắp xếp theo ngày tạo mới nhất
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<StockImport> importPage;

        if ((search != null && !search.isEmpty()) || status != null || fromDateTime != null || toDateTime != null) {
            importPage = stockImportService.searchStockImports(search, status, fromDateTime, toDateTime, pageable);
        } else {
            importPage = stockImportService.getAllStockImports(pageable);
        }

        // Thống kê
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", stockImportService.getTotalImports());
        stats.put("pending", stockImportService.countByStatus(StockImport.ImportStatus.PENDING));
        stats.put("completed", stockImportService.countByStatus(StockImport.ImportStatus.COMPLETED));
        stats.put("cancelled", stockImportService.countByStatus(StockImport.ImportStatus.CANCEllED));

        model.addAttribute("imports", importPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", importPage.getTotalPages());
        model.addAttribute("totalItems", importPage.getTotalElements());
        model.addAttribute("stats", stats);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("filterStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "stock/import_stock";
    }

    /**
     * Hiển thị form tạo phiếu nhập mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("stockImport", new StockImportDTO());
        model.addAttribute("suppliers", supplierService.getAllActiveSuppliers());
        model.addAttribute("devices", medicalDeviceService.getAllActiveDevices());
        model.addAttribute("isEdit", false);
        return "admin/stock-import/form";
    }

    /**
     * Xử lý tạo phiếu nhập mới
     */
    @PostMapping("/create")
    public String createStockImport(
            @ModelAttribute StockImportDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            StockImport stockImport = stockImportService.createStockImport(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo phiếu nhập kho " + stockImport.getImportCode() + " thành công!");
            return "redirect:/admin/stock-import/" + stockImport.getImportId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi tạo phiếu nhập: " + e.getMessage());
            return "redirect:/admin/stock-import/create";
        }
    }

    /**
     * Hiển thị chi tiết phiếu nhập
     */
    @GetMapping("/{id}")
    public String viewStockImport(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            StockImport stockImport = stockImportService.getStockImportById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với ID: " + id));

            model.addAttribute("stockImport", stockImport);
            return "admin/stock-import/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/stock-import";
        }
    }

    /**
     * Hiển thị form sửa phiếu nhập
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            StockImport stockImport = stockImportService.getStockImportById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với ID: " + id));

            // Chỉ cho phép sửa phiếu đang chờ duyệt
            if (stockImport.getStatus() != StockImport.ImportStatus.PENDING) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Chỉ có thể sửa phiếu nhập đang chờ duyệt!");
                return "redirect:/admin/stock-import/" + id;
            }

            StockImportDTO dto = stockImportService.convertToDTO(stockImport);
            model.addAttribute("stockImport", dto);
            model.addAttribute("suppliers", supplierService.getAllActiveSuppliers());
            model.addAttribute("devices", medicalDeviceService.getAllActiveDevices());
            model.addAttribute("isEdit", true);

            return "admin/stock-import/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/stock-import";
        }
    }

    /**
     * Xử lý cập nhật phiếu nhập
     */
    @PostMapping("/edit/{id}")
    public String updateStockImport(
            @PathVariable Integer id,
            @ModelAttribute StockImportDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            StockImport stockImport = stockImportService.updateStockImport(id, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật phiếu nhập " + stockImport.getImportCode() + " thành công!");
            return "redirect:/admin/stock-import/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi cập nhật phiếu nhập: " + e.getMessage());
            return "redirect:/admin/stock-import/edit/" + id;
        }
    }

    /**
     * Duyệt phiếu nhập (cập nhật tồn kho)
     */
    @PostMapping("/approve/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveStockImport(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            StockImport stockImport = stockImportService.approveStockImport(id);
            response.put("success", true);
            response.put("message", "Đã duyệt phiếu nhập " + stockImport.getImportCode() + " thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Hủy phiếu nhập
     */
    @PostMapping("/cancel/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelStockImport(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String reason = payload.get("reason");
            StockImport stockImport = stockImportService.cancelStockImport(id, reason);
            response.put("success", true);
            response.put("message", "Đã hủy phiếu nhập " + stockImport.getImportCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa phiếu nhập (chỉ xóa phiếu đã hủy)
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteStockImport(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            stockImportService.deleteStockImport(id);
            response.put("success", true);
            response.put("message", "Đã xóa phiếu nhập thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * In phiếu nhập
     */
    @GetMapping("/print/{id}")
    public String printStockImport(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            StockImport stockImport = stockImportService.getStockImportById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập với ID: " + id));

            model.addAttribute("stockImport", stockImport);
            return "admin/stock-import/print";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/stock-import";
        }
    }

    /**
     * Xuất báo cáo Excel
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

            byte[] excelData = stockImportService.exportToExcel(search, status, fromDateTime, toDateTime);

            String filename = "phieu-nhap-kho_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi xuất Excel: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API lấy thông tin thiết bị theo ID (cho form tạo/sửa)
     */
    @GetMapping("/api/device/{id}")
    @ResponseBody
    public ResponseEntity<?> getDeviceInfo(@PathVariable String id) {
        try {
            var device = medicalDeviceService.getDeviceById(id);
            Map<String, Object> info = new HashMap<>();
            info.put("deviceId", device.getDeviceId());
            info.put("deviceName", device.getName());
            info.put("currentStock", device.getStockQuantity());
            info.put("imageUrl", device.getImageUrl());
            // Có thể thêm giá nhập gần nhất nếu cần
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không tìm thấy thiết bị");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API tìm kiếm thiết bị (cho autocomplete)
     */
    @GetMapping("/api/devices/search")
    @ResponseBody
    public ResponseEntity<?> searchDevices(@RequestParam String keyword) {
        try {
            var devices = medicalDeviceService.searchDevicesByName(keyword);
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi khi tìm kiếm thiết bị");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
