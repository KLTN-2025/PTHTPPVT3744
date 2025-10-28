package com.example.do_an_tot_nghiep.repository;

import com.example.do_an_tot_nghiep.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBrandRepository extends JpaRepository<Brand, Integer> {
    /**
     * Tìm thương hiệu theo tên
     */
    Brand findByName(String name);

    /**
     * Lấy thương hiệu đang hoạt động
     */
    List<Brand> findByIsActiveTrue();
}

/**
 * OrderRepository - Quản lý đơn hàng (nếu cần)
 */

