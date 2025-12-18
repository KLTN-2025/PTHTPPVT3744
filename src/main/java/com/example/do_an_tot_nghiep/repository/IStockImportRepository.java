package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.StockImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface IStockImportRepository extends JpaRepository<StockImport, Integer> {

    // ✅ Đếm số đơn nhập hàng (dùng native query)
    @Query(value = "SELECT COUNT(*) FROM stock_import WHERE supplier_id = :supplierId",
            nativeQuery = true)
    Long countBySupplierId(@Param("supplierId") Integer supplierId);

    // ✅ Tính tổng giá trị (dùng native query, chú ý status = 'COMPLETED' hoặc 'Completed')
    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM stock_import " +
            "WHERE supplier_id = :supplierId AND status = 'COMPLETED'",
            nativeQuery = true)
    BigDecimal sumTotalAmountBySupplierIdAndStatusCompleted(@Param("supplierId") Integer supplierId);
}