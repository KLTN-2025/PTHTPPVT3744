package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISupplierRepository extends JpaRepository<Supplier, Integer> {

    // Tìm theo tên
    Optional<Supplier> findByName(String name);

    // Tìm theo mã số thuế
    Optional<Supplier> findByTaxCode(String taxCode);

    // Tìm theo email
    Optional<Supplier> findByEmail(String email);

    // Tìm theo trạng thái
    List<Supplier> findByStatus(Supplier.SupplierStatus status);

    // Tìm theo trạng thái với phân trang
    Page<Supplier> findByStatus(Supplier.SupplierStatus status, Pageable pageable);

    // Tìm kiếm theo tên hoặc người liên hệ
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Supplier> searchSuppliers(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm theo keyword và status
    @Query("SELECT s FROM Supplier s WHERE " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "s.status = :status")
    Page<Supplier> searchSuppliersByStatus(
            @Param("keyword") String keyword,
            @Param("status") Supplier.SupplierStatus status,
            Pageable pageable
    );

    // Kiểm tra tên đã tồn tại (ngoại trừ ID hiện tại khi update)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s " +
            "WHERE s.name = :name AND (:supplierId IS NULL OR s.supplierId != :supplierId)")
    boolean existsByNameAndNotId(@Param("name") String name, @Param("supplierId") Integer supplierId);

    // Kiểm tra mã số thuế đã tồn tại
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s " +
            "WHERE s.taxCode = :taxCode AND (:supplierId IS NULL OR s.supplierId != :supplierId)")
    boolean existsByTaxCodeAndNotId(@Param("taxCode") String taxCode, @Param("supplierId") Integer supplierId);

    // Đếm số nhà cung cấp theo trạng thái
    long countByStatus(Supplier.SupplierStatus status);
    // Lấy tất cả nhà cung cấp đang hoạt động
    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE'")
    List<Supplier> findAllActiveSuppliers();
}