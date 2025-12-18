package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.BrandDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBrandService {

    /**
     * Lấy tất cả brands (không phân trang)
     */
    List<BrandDTO> getAllBrands();

    /**
     * Lấy tất cả brands có phân trang
     */
    Page<BrandDTO> getAllBrandsPage(Pageable pageable);

    /**
     * Lấy brands đang active
     */
    List<BrandDTO> getActiveBrands();

    /**
     * Tìm kiếm brands
     */
    Page<BrandDTO> searchBrands(String keyword, Pageable pageable);

    /**
     * Lọc brands theo trạng thái
     */
    Page<BrandDTO> getBrandsByStatus(Boolean isActive, Pageable pageable);

    /**
     * Lấy brand theo ID
     */
    BrandDTO getBrandById(Integer id);

    /**
     * Tạo brand mới
     */
    BrandDTO createBrand(BrandDTO dto, MultipartFile logoFile);

    /**
     * Cập nhật brand
     */
    BrandDTO updateBrand(Integer id, BrandDTO dto, MultipartFile logoFile);

    /**
     * Xóa brand đơn
     */
    void deleteBrand(Integer id);

    /**
     * Xóa nhiều brands
     */
    int deleteBrands(List<Integer> ids);

    /**
     * Bật/tắt trạng thái
     */
    void toggleStatus(Integer id);

    /**
     * Kiểm tra slug tồn tại
     */
    boolean isSlugExists(String slug, Integer excludeId);

    /**
     * Kiểm tra tên tồn tại
     */
    boolean isNameExists(String name, Integer excludeId);

    /**
     * Đếm số sản phẩm của brand
     */
    long countProducts(Integer brandId);

    /**
     * Lấy brands theo quốc gia
     */
    List<BrandDTO> getBrandsByCountry(String country);
}