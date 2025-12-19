package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.SupplierDTO;
import com.example.do_an_tot_nghiep.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ISupplierService {

    /**
     * Lấy tất cả nhà cung cấp (không phân trang)
     * @return Danh sách tất cả nhà cung cấp
     */
    List<SupplierDTO> getAllSuppliers();

    /**
     * Lấy danh sách nhà cung cấp có phân trang
     * @param pageable Thông tin phân trang
     * @return Page của SupplierDTO
     */
    Page<SupplierDTO> getAllSuppliers(Pageable pageable);

    /**
     * Lấy thông tin nhà cung cấp theo ID
     * @param id ID của nhà cung cấp
     * @return SupplierDTO
     * @throws RuntimeException nếu không tìm thấy
     */
    SupplierDTO getSupplierById(Integer id);

    /**
     * Tạo mới nhà cung cấp
     * @param supplierDTO Thông tin nhà cung cấp
     * @return SupplierDTO đã được tạo
     * @throws RuntimeException nếu tên hoặc mã số thuế đã tồn tại
     */
    SupplierDTO createSupplier(SupplierDTO supplierDTO);

    /**
     * Cập nhật thông tin nhà cung cấp
     * @param id ID của nhà cung cấp cần cập nhật
     * @param supplierDTO Thông tin mới
     * @return SupplierDTO đã được cập nhật
     * @throws RuntimeException nếu không tìm thấy hoặc dữ liệu trùng lặp
     */
    SupplierDTO updateSupplier(Integer id, SupplierDTO supplierDTO);

    /**
     * Xóa nhà cung cấp
     * @param id ID của nhà cung cấp cần xóa
     * @throws RuntimeException nếu không tìm thấy
     */
    void deleteSupplier(Integer id);

    /**
     * Vô hiệu hóa nhà cung cấp (soft delete)
     * @param id ID của nhà cung cấp
     * @return SupplierDTO đã được vô hiệu hóa
     * @throws RuntimeException nếu không tìm thấy
     */
    SupplierDTO deactivateSupplier(Integer id);

    /**
     * Kích hoạt nhà cung cấp
     * @param id ID của nhà cung cấp
     * @return SupplierDTO đã được kích hoạt
     * @throws RuntimeException nếu không tìm thấy
     */
    SupplierDTO activateSupplier(Integer id);

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page của SupplierDTO
     */
    Page<SupplierDTO> searchSuppliers(String keyword, Pageable pageable);

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa và trạng thái
     * @param keyword Từ khóa tìm kiếm
     * @param status Trạng thái (ACTIVE, INACTIVE, ALL)
     * @param pageable Thông tin phân trang
     * @return Page của SupplierDTO
     */
    Page<SupplierDTO> searchSuppliersByStatus(String keyword, String status, Pageable pageable);

    /**
     * Lấy danh sách nhà cung cấp theo trạng thái
     * @param status Trạng thái (ACTIVE hoặc INACTIVE)
     * @return Danh sách nhà cung cấp
     */
    List<SupplierDTO> getSuppliersByStatus(String status);

    /**
     * Đếm số lượng nhà cung cấp theo trạng thái
     * @param status Trạng thái (ACTIVE hoặc INACTIVE)
     * @return Số lượng nhà cung cấp
     */
    long countByStatus(String status);

    /**
     * Lấy tổng số nhà cung cấp
     * @return Tổng số nhà cung cấp
     */
    long getTotalSuppliers();

    // ✅ THÊM MỚI
    /**
     * Lấy thống kê của nhà cung cấp
     * @param supplierId ID của nhà cung cấp
     * @return Map chứa các thông tin thống kê (productCount, importCount, totalValue)
     */
    Map<String, Object> getSupplierStatistics(Integer supplierId);

    List<Supplier> getAllActiveSuppliers();
}