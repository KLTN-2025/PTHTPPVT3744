package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.SupplierDTO;
import com.example.do_an_tot_nghiep.model.Supplier;
import com.example.do_an_tot_nghiep.repository.IMedicalDeviceRepository;
import com.example.do_an_tot_nghiep.repository.IStockImportRepository;
import com.example.do_an_tot_nghiep.repository.ISupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService implements ISupplierService {

    private final ISupplierRepository supplierRepository;
    private final IMedicalDeviceRepository medicalDeviceRepository; // ✅ THÊM
    private final IStockImportRepository stockImportRepository;     // ✅ THÊM

    /**
     * Lấy tất cả nhà cung cấp
     */
    @Override
    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(SupplierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách nhà cung cấp có phân trang
     */
    @Override
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(SupplierDTO::fromEntity);
    }

    /**
     * Lấy nhà cung cấp theo ID
     */
    @Override
    public SupplierDTO getSupplierById(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));
        return SupplierDTO.fromEntity(supplier);
    }

    /**
     * Tạo mới nhà cung cấp
     */
    @Override
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        // Kiểm tra tên đã tồn tại
        if (supplierRepository.existsByNameAndNotId(supplierDTO.getName(), null)) {
            throw new RuntimeException("Tên nhà cung cấp đã tồn tại");
        }

        // Kiểm tra mã số thuế đã tồn tại
        if (supplierDTO.getTaxCode() != null && !supplierDTO.getTaxCode().isEmpty()) {
            if (supplierRepository.existsByTaxCodeAndNotId(supplierDTO.getTaxCode(), null)) {
                throw new RuntimeException("Mã số thuế đã tồn tại");
            }
        }

        Supplier supplier = supplierDTO.toEntity();
        if (supplier.getStatus() == null) {
            supplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        }

        Supplier savedSupplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(savedSupplier);
    }

    /**
     * Cập nhật nhà cung cấp
     */
    @Override
    public SupplierDTO updateSupplier(Integer id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        // Kiểm tra tên đã tồn tại (trừ chính nó)
        if (supplierRepository.existsByNameAndNotId(supplierDTO.getName(), id)) {
            throw new RuntimeException("Tên nhà cung cấp đã tồn tại");
        }

        // Kiểm tra mã số thuế đã tồn tại (trừ chính nó)
        if (supplierDTO.getTaxCode() != null && !supplierDTO.getTaxCode().isEmpty()) {
            if (supplierRepository.existsByTaxCodeAndNotId(supplierDTO.getTaxCode(), id)) {
                throw new RuntimeException("Mã số thuế đã tồn tại");
            }
        }

        // Cập nhật thông tin
        existingSupplier.setName(supplierDTO.getName());
        existingSupplier.setContactPerson(supplierDTO.getContactPerson());
        existingSupplier.setAddress(supplierDTO.getAddress());
        existingSupplier.setPhone(supplierDTO.getPhone());
        existingSupplier.setEmail(supplierDTO.getEmail());
        existingSupplier.setTaxCode(supplierDTO.getTaxCode());
        existingSupplier.setBankAccount(supplierDTO.getBankAccount());
        existingSupplier.setBankName(supplierDTO.getBankName());
        existingSupplier.setDescription(supplierDTO.getDescription());

        if (supplierDTO.getStatus() != null) {
            existingSupplier.setStatus(Supplier.SupplierStatus.valueOf(supplierDTO.getStatus()));
        }

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return SupplierDTO.fromEntity(updatedSupplier);
    }

    /**
     * Xóa nhà cung cấp
     */
    @Override
    public void deleteSupplier(Integer id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id);
        }
        supplierRepository.deleteById(id);
    }

    /**
     * Vô hiệu hóa nhà cung cấp (soft delete)
     */
    @Override
    public SupplierDTO deactivateSupplier(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        supplier.setStatus(Supplier.SupplierStatus.INACTIVE);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(updatedSupplier);
    }

    /**
     * Kích hoạt nhà cung cấp
     */
    @Override
    public SupplierDTO activateSupplier(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà cung cấp với ID: " + id));

        supplier.setStatus(Supplier.SupplierStatus.ACTIVE);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(updatedSupplier);
    }

    /**
     * Tìm kiếm nhà cung cấp
     */
    @Override
    public Page<SupplierDTO> searchSuppliers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSuppliers(pageable);
        }
        return supplierRepository.searchSuppliers(keyword.trim(), pageable)
                .map(SupplierDTO::fromEntity);
    }

    /**
     * Tìm kiếm nhà cung cấp theo trạng thái
     */
    @Override
    public Page<SupplierDTO> searchSuppliersByStatus(String keyword, String status, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status);
                return supplierRepository.findByStatus(supplierStatus, pageable)
                        .map(SupplierDTO::fromEntity);
            }
            return getAllSuppliers(pageable);
        }

        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status);
            return supplierRepository.searchSuppliersByStatus(keyword.trim(), supplierStatus, pageable)
                    .map(SupplierDTO::fromEntity);
        }

        return searchSuppliers(keyword, pageable);
    }

    /**
     * Lấy nhà cung cấp theo trạng thái
     */
    @Override
    public List<SupplierDTO> getSuppliersByStatus(String status) {
        Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status);
        return supplierRepository.findByStatus(supplierStatus).stream()
                .map(SupplierDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lượng nhà cung cấp theo trạng thái
     */
    @Override
    public long countByStatus(String status) {
        Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status);
        return supplierRepository.countByStatus(supplierStatus);
    }

    /**
     * Lấy tổng số nhà cung cấp
     */
    @Override
    public long getTotalSuppliers() {
        return supplierRepository.count();
    }

    // ✅ THÊM METHOD MỚI
    /**
     * Lấy thống kê của nhà cung cấp
     */
    @Override
    public Map<String, Object> getSupplierStatistics(Integer supplierId) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // Đếm số sản phẩm
            Long productCount = medicalDeviceRepository.countBySupplierId(supplierId);

            // Đếm số đơn nhập hàng
            Long importCount = stockImportRepository.countBySupplierId(supplierId);

            // Tính tổng giá trị
            BigDecimal totalValue = stockImportRepository
                    .sumTotalAmountBySupplierIdAndStatusCompleted(supplierId);

            statistics.put("productCount", productCount != null ? productCount : 0L);
            statistics.put("importCount", importCount != null ? importCount : 0L);
            statistics.put("totalValue", totalValue != null ? totalValue : BigDecimal.ZERO);

        } catch (Exception e) {
            // Xử lý lỗi, trả về giá trị mặc định
            statistics.put("productCount", 0L);
            statistics.put("importCount", 0L);
            statistics.put("totalValue", BigDecimal.ZERO);
        }

        return statistics;
    }

    @Override
    public List<Supplier> getAllActiveSuppliers() {
        return supplierRepository.findAllActiveSuppliers();
    }
}