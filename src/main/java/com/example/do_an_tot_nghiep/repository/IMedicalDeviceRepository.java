package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.MedicalDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IMedicalDeviceRepository extends JpaRepository<MedicalDevice, String> {
    @Query("SELECT d FROM MedicalDevice d WHERE d.stockQuantity <= d.minStockLevel " +
            "AND d.status != 'Ngừng bán' ORDER BY (d.minStockLevel - d.stockQuantity) DESC")
    List<MedicalDevice> findLowStockProducts();

    @Query("SELECT COUNT(d) FROM MedicalDevice d WHERE d.stockQuantity <= d.minStockLevel")
    Long countLowStockProducts();

    List<MedicalDevice> findByIsFeaturedTrue();

    List<MedicalDevice> findByIsNewTrue();

    @Query("SELECT d FROM MedicalDevice d ORDER BY d.soldCount DESC")
    List<MedicalDevice> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
