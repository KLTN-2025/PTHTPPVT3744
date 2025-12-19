package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.StockImportDTO;
import com.example.do_an_tot_nghiep.dto.StockImportDetailDTO;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockImportService {

    private final IStockImportRepository stockImportRepository;
    private final IStockImportDetailRepository stockImportDetailRepository;
    private final ISupplierRepository supplierRepository;
    private final IMedicalDeviceRepository medicalDeviceRepository;
    private final IEmployeeRepository employeeRepository;

    /**
     * Lấy tất cả phiếu nhập có phân trang
     */
    public Page<StockImport> getAllStockImports(Pageable pageable) {
        return stockImportRepository.findAll(pageable);
    }

    /**
     * Tìm kiếm phiếu nhập theo các tiêu chí
     */
    public Page<StockImport> searchStockImports(
            String search,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Specification<StockImport> spec = null;

        // Tìm theo mã phiếu hoặc tên nhà cung cấp
        if (search != null && !search.isEmpty()) {
            Specification<StockImport> searchSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("importCode")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("supplier").get("supplierName")), "%" + search.toLowerCase() + "%")
                    );
            spec = (spec == null) ? searchSpec : spec.and(searchSpec);
        }

        // Lọc theo trạng thái
        if (status != null && !status.isEmpty()) {
            Specification<StockImport> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), StockImport.ImportStatus.valueOf(status));
            spec = (spec == null) ? statusSpec : spec.and(statusSpec);
        }

        // Lọc theo khoảng thời gian
        if (fromDate != null) {
            Specification<StockImport> fromDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("importDate"), fromDate);
            spec = (spec == null) ? fromDateSpec : spec.and(fromDateSpec);
        }
        if (toDate != null) {
            Specification<StockImport> toDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("importDate"), toDate);
            spec = (spec == null) ? toDateSpec : spec.and(toDateSpec);
        }

        return stockImportRepository.findAll(spec, pageable);
    }

    /**
     * Lấy phiếu nhập theo ID
     */
    public Optional<StockImport> getStockImportById(Integer id) {
        return stockImportRepository.findById(id);
    }

    /**
     * Tạo phiếu nhập mới
     */
    @Transactional
    public StockImport createStockImport(StockImportDTO dto) {
        // Validate
        validateStockImportDTO(dto);

        // Lấy nhà cung cấp
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        // Lấy nhân viên hiện tại
        Employee currentEmployee = getCurrentEmployee();

        // Tạo mã phiếu nhập tự động
        String importCode = generateImportCode();

        // Tạo phiếu nhập
        StockImport stockImport = StockImport.builder()
                .importCode(importCode)
                .supplier(supplier)
                .importDate(dto.getImportDate() != null ? dto.getImportDate() : LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .note(dto.getNote())
                .status(StockImport.ImportStatus.PENDING)
                .createdBy(currentEmployee)
                .build();

        stockImport = stockImportRepository.save(stockImport);

        // Tạo chi tiết phiếu nhập
        List<StockImportDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (StockImportDetailDTO detailDTO : dto.getImportDetails()) {
            MedicalDevice device = medicalDeviceRepository.findById(String.valueOf(detailDTO.getDeviceId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + detailDTO.getDeviceId()));

            BigDecimal detailTotal = detailDTO.getImportPrice().multiply(new BigDecimal(detailDTO.getQuantity()));
            totalAmount = totalAmount.add(detailTotal);

            StockImportDetail detail = StockImportDetail.builder()
                    .stockImport(stockImport)
                    .device(device)
                    .quantity(detailDTO.getQuantity())
                    .importPrice(detailDTO.getImportPrice())
                    .totalPrice(detailTotal)
                    .expiryDate(detailDTO.getExpiryDate())
                    .batchNumber(detailDTO.getBatchNumber())
                    .build();

            details.add(detail);
        }

        stockImportDetailRepository.saveAll(details);

        // Cập nhật tổng tiền
        stockImport.setTotalAmount(totalAmount);
        stockImport.setImportDetails(details);

        return stockImportRepository.save(stockImport);
    }

    /**
     * Cập nhật phiếu nhập
     */
    @Transactional
    public StockImport updateStockImport(Integer id, StockImportDTO dto) {
        StockImport stockImport = stockImportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        // Chỉ cho phép sửa phiếu đang chờ duyệt
        if (stockImport.getStatus() != StockImport.ImportStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể sửa phiếu nhập đang chờ duyệt");
        }

        // Validate
        validateStockImportDTO(dto);

        // Cập nhật thông tin
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp"));

        stockImport.setSupplier(supplier);
        stockImport.setImportDate(dto.getImportDate());
        stockImport.setNote(dto.getNote());

        // Xóa chi tiết cũ
        stockImportDetailRepository.deleteAll(stockImport.getImportDetails());

        // Tạo chi tiết mới
        List<StockImportDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (StockImportDetailDTO detailDTO : dto.getImportDetails()) {
            MedicalDevice device = medicalDeviceRepository.findById(String.valueOf(detailDTO.getDeviceId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị ID: " + detailDTO.getDeviceId()));

            BigDecimal detailTotal = detailDTO.getImportPrice().multiply(new BigDecimal(detailDTO.getQuantity()));
            totalAmount = totalAmount.add(detailTotal);

            StockImportDetail detail = StockImportDetail.builder()
                    .stockImport(stockImport)
                    .device(device)
                    .quantity(detailDTO.getQuantity())
                    .importPrice(detailDTO.getImportPrice())
                    .totalPrice(detailTotal)
                    .expiryDate(detailDTO.getExpiryDate())
                    .batchNumber(detailDTO.getBatchNumber())
                    .build();

            details.add(detail);
        }

        stockImportDetailRepository.saveAll(details);

        stockImport.setTotalAmount(totalAmount);
        stockImport.setImportDetails(details);

        return stockImportRepository.save(stockImport);
    }

    /**
     * Duyệt phiếu nhập - cập nhật tồn kho
     */
    @Transactional
    public StockImport approveStockImport(Integer id) {
        StockImport stockImport = stockImportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        if (stockImport.getStatus() != StockImport.ImportStatus.PENDING) {
            throw new RuntimeException("Phiếu nhập này đã được xử lý");
        }

        Employee currentEmployee = getCurrentEmployee();

        // Cập nhật tồn kho cho từng thiết bị
        for (StockImportDetail detail : stockImport.getImportDetails()) {
            MedicalDevice device = detail.getDevice();
            Integer currentStock = device.getStockQuantity() != null ? device.getStockQuantity() : 0;
            device.setStockQuantity(currentStock + detail.getQuantity());
            medicalDeviceRepository.save(device);
        }

        // Cập nhật trạng thái phiếu nhập
        stockImport.setStatus(StockImport.ImportStatus.COMPLETED);
        stockImport.setApprovedBy(currentEmployee);
        stockImport.setApprovedAt(LocalDateTime.now());

        return stockImportRepository.save(stockImport);
    }

    /**
     * Hủy phiếu nhập
     */
    @Transactional
    public StockImport cancelStockImport(Integer id, String reason) {
        StockImport stockImport = stockImportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        if (stockImport.getStatus() != StockImport.ImportStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy phiếu nhập đang chờ duyệt");
        }

        stockImport.setStatus(StockImport.ImportStatus.CANCEllED);
        stockImport.setNote(stockImport.getNote() + "\n[HỦY] " + reason);

        return stockImportRepository.save(stockImport);
    }

    /**
     * Xóa phiếu nhập (chỉ xóa phiếu đã hủy)
     */
    @Transactional
    public void deleteStockImport(Integer id) {
        StockImport stockImport = stockImportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        if (stockImport.getStatus() != StockImport.ImportStatus.CANCEllED) {
            throw new RuntimeException("Chỉ có thể xóa phiếu nhập đã hủy");
        }

        stockImportRepository.delete(stockImport);
    }

    /**
     * Chuyển đổi Entity sang DTO
     */
    public StockImportDTO convertToDTO(StockImport stockImport) {
        List<StockImportDetailDTO> detailDTOs = stockImport.getImportDetails().stream()
                .map(detail -> StockImportDetailDTO.builder()
                        .importDetailId(detail.getImportDetailId())
                        .importId(stockImport.getImportId())
                        .deviceId(Integer.valueOf(detail.getDevice().getDeviceId()))
                        .deviceName(detail.getDevice().getName())
                        .deviceImage(detail.getDevice().getImageUrl())
                        .quantity(detail.getQuantity())
                        .importPrice(detail.getImportPrice())
                        .totalPrice(detail.getTotalPrice())
                        .expiryDate(detail.getExpiryDate())
                        .batchNumber(detail.getBatchNumber())
                        .build())
                .collect(Collectors.toList());

        return StockImportDTO.builder()
                .importId(stockImport.getImportId())
                .importCode(stockImport.getImportCode())
                .supplierId(stockImport.getSupplier().getSupplierId())
                .supplierName(stockImport.getSupplier().getName())
                .importDate(stockImport.getImportDate())
                .totalAmount(stockImport.getTotalAmount())
                .note(stockImport.getNote())
                .status(stockImport.getStatus().name())
                .createdById(stockImport.getCreatedBy().getEmployeeId())
                .createdByName(stockImport.getCreatedBy().getFullName())
                .approvedById(stockImport.getApprovedBy() != null ? stockImport.getApprovedBy().getEmployeeId() : null)
                .approvedByName(stockImport.getApprovedBy() != null ? stockImport.getApprovedBy().getFullName() : null)
                .approvedAt(stockImport.getApprovedAt())
                .createdAt(stockImport.getCreatedAt())
                .updatedAt(stockImport.getUpdatedAt())
                .importDetails(detailDTOs)
                .build();
    }

    /**
     * Tạo mã phiếu nhập tự động
     */
    private String generateImportCode() {
        String prefix = "IMP-";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Long count = stockImportRepository.countByImportCodeStartingWith(prefix + date);
        String sequence = String.format("%04d", count + 1);

        return prefix + date + "-" + sequence;
    }

    /**
     * Validate DTO
     */
    private void validateStockImportDTO(StockImportDTO dto) {
        if (dto.getSupplierId() == null) {
            throw new RuntimeException("Vui lòng chọn nhà cung cấp");
        }

        if (dto.getImportDetails() == null || dto.getImportDetails().isEmpty()) {
            throw new RuntimeException("Vui lòng thêm ít nhất một thiết bị");
        }

        for (StockImportDetailDTO detail : dto.getImportDetails()) {
            if (detail.getDeviceId() == null) {
                throw new RuntimeException("Vui lòng chọn thiết bị");
            }
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new RuntimeException("Số lượng phải lớn hơn 0");
            }
            if (detail.getImportPrice() == null || detail.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Giá nhập phải lớn hơn 0");
            }
        }
    }

    /**
     * Lấy nhân viên hiện tại
     */
    private Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
    }

    /**
     * Thống kê
     */
    public Long getTotalImports() {
        return stockImportRepository.count();
    }

    public Long countByStatus(StockImport.ImportStatus status) {
        return stockImportRepository.countByStatus(status);
    }

    /**
     * Xuất Excel
     */
    public byte[] exportToExcel(String search, String status, LocalDateTime fromDate, LocalDateTime toDate) throws Exception {
        Specification<StockImport> spec = null;

        if (search != null && !search.isEmpty()) {
            Specification<StockImport> searchSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("importCode")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("supplier").get("supplierName")), "%" + search.toLowerCase() + "%")
                    );
            spec = (spec == null) ? searchSpec : spec.and(searchSpec);
        }

        if (status != null && !status.isEmpty()) {
            Specification<StockImport> statusSpec = (root, query, cb) ->
                    cb.equal(root.get("status"), StockImport.ImportStatus.valueOf(status));
            spec = (spec == null) ? statusSpec : spec.and(statusSpec);
        }

        if (fromDate != null) {
            Specification<StockImport> fromDateSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("importDate"), fromDate);
            spec = (spec == null) ? fromDateSpec : spec.and(fromDateSpec);
        }
        if (toDate != null) {
            Specification<StockImport> toDateSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("importDate"), toDate);
            spec = (spec == null) ? toDateSpec : spec.and(toDateSpec);
        }

        List<StockImport> imports = stockImportRepository.findAll((Sort) spec);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Phiếu nhập kho");

        // Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Mã phiếu", "Nhà cung cấp", "Ngày nhập", "Người tạo", "Tổng tiền", "Trạng thái", "Người duyệt", "Ngày duyệt"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Data
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (StockImport imp : imports) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(imp.getImportCode());
            row.createCell(1).setCellValue(imp.getSupplier().getName());
            row.createCell(2).setCellValue(imp.getImportDate().format(formatter));
            row.createCell(3).setCellValue(imp.getCreatedBy().getFullName());
            row.createCell(4).setCellValue(imp.getTotalAmount().doubleValue());
            row.createCell(5).setCellValue(getStatusText(imp.getStatus()));
            row.createCell(6).setCellValue(imp.getApprovedBy() != null ? imp.getApprovedBy().getFullName() : "");
            row.createCell(7).setCellValue(imp.getApprovedAt() != null ? imp.getApprovedAt().format(formatter) : "");
        }

        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private String getStatusText(StockImport.ImportStatus status) {
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case COMPLETED: return "Đã hoàn thành";
            case CANCEllED: return "Đã hủy";
            default: return "";
        }
    }
}