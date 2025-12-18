package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.BrandDTO;
import com.example.do_an_tot_nghiep.model.Brand;
import com.example.do_an_tot_nghiep.repository.IBrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService implements IBrandService {

    private final IBrandRepository brandRepository;
    private final FileUploadService fileUploadService;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BrandDTO> getAllBrandsPage(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<BrandDTO> getActiveBrands() {
        return brandRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BrandDTO> searchBrands(String keyword, Pageable pageable) {
        return brandRepository.searchBrands(keyword, null, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<BrandDTO> getBrandsByStatus(Boolean isActive, Pageable pageable) {
        return brandRepository.findByIsActive(isActive, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public BrandDTO getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        return convertToDTO(brand);
    }

    @Override
    @Transactional
    public BrandDTO createBrand(BrandDTO dto, MultipartFile logoFile) {
        log.info("Creating new brand: {}", dto.getName());

        // Kiểm tra tên đã tồn tại
        if (isNameExists(dto.getName(), null)) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setName(dto.getName());

        // Tự động tạo slug
        String slug = (dto.getSlug() == null || dto.getSlug().isEmpty())
                ? generateSlug(dto.getName())
                : dto.getSlug();

        if (isSlugExists(slug, null)) {
            throw new RuntimeException("Slug đã tồn tại");
        }
        brand.setSlug(slug);

        brand.setDescription(dto.getDescription());
        brand.setWebsite(dto.getWebsite());
        brand.setCountry(dto.getCountry());
        brand.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Upload logo
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoUrl = fileUploadService.uploadFile(logoFile, "brands");
                brand.setLogoUrl(logoUrl);
                log.info("Uploaded brand logo: {}", logoUrl);
            } catch (Exception e) {
                log.error("Error uploading logo", e);
                throw new RuntimeException("Lỗi khi upload logo: " + e.getMessage());
            }
        }

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand successfully with id: {}", savedBrand.getBrandId());
        return convertToDTO(savedBrand);
    }

    @Override
    @Transactional
    public BrandDTO updateBrand(Integer id, BrandDTO dto, MultipartFile logoFile) {
        log.info("Updating brand id: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        // Kiểm tra tên
        if (isNameExists(dto.getName(), id)) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        brand.setName(dto.getName());

        // Update slug
        String slug = (dto.getSlug() == null || dto.getSlug().isEmpty())
                ? generateSlug(dto.getName())
                : dto.getSlug();

        if (isSlugExists(slug, id)) {
            throw new RuntimeException("Slug đã tồn tại");
        }
        brand.setSlug(slug);

        brand.setDescription(dto.getDescription());
        brand.setWebsite(dto.getWebsite());
        brand.setCountry(dto.getCountry());
        brand.setIsActive(dto.getIsActive());

        // Upload logo mới
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                // Xóa logo cũ
                if (brand.getLogoUrl() != null) {
                    fileUploadService.deleteFile(brand.getLogoUrl());
                }
                String logoUrl = fileUploadService.uploadFile(logoFile, "brands");
                brand.setLogoUrl(logoUrl);
                log.info("Updated brand logo: {}", logoUrl);
            } catch (Exception e) {
                log.error("Error uploading logo", e);
                throw new RuntimeException("Lỗi khi upload logo: " + e.getMessage());
            }
        }

        Brand updatedBrand = brandRepository.save(brand);
        return convertToDTO(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        // Kiểm tra có sản phẩm không
        if (brandRepository.hasProducts(id)) {
            throw new RuntimeException("Không thể xóa thương hiệu đang có sản phẩm");
        }

        // Xóa logo
        if (brand.getLogoUrl() != null) {
            try {
                fileUploadService.deleteFile(brand.getLogoUrl());
            } catch (Exception e) {
                log.error("Error deleting logo", e);
            }
        }

        brandRepository.delete(brand);
        log.info("Deleted brand id: {}", id);
    }

    @Override
    @Transactional
    public int deleteBrands(List<Integer> ids) {
        int deletedCount = 0;
        StringBuilder errors = new StringBuilder();

        for (Integer id : ids) {
            try {
                deleteBrand(id);
                deletedCount++;
            } catch (Exception e) {
                errors.append("ID ").append(id).append(": ").append(e.getMessage()).append("\n");
                log.error("Failed to delete brand id: {}", id, e);
            }
        }

        if (errors.length() > 0 && deletedCount == 0) {
            throw new RuntimeException("Không thể xóa thương hiệu:\n" + errors.toString());
        }

        return deletedCount;
    }

    @Override
    @Transactional
    public void toggleStatus(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        brand.setIsActive(!brand.getIsActive());
        brandRepository.save(brand);
        log.info("Toggled brand status to: {}", brand.getIsActive());
    }

    @Override
    public boolean isSlugExists(String slug, Integer excludeId) {
        Brand existing = brandRepository.findBySlug(slug);
        if (existing == null) {
            return false;
        }
        return excludeId == null || !existing.getBrandId().equals(excludeId);
    }

    @Override
    public boolean isNameExists(String name, Integer excludeId) {
        boolean exists = brandRepository.existsByName(name);
        if (!exists) {
            return false;
        }

        if (excludeId != null) {
            Brand existing = brandRepository.findById(excludeId).orElse(null);
            return existing == null || !existing.getName().equals(name);
        }

        return true;
    }

    @Override
    public long countProducts(Integer brandId) {
        return brandRepository.countProductsByBrand(brandId);
    }

    @Override
    public List<BrandDTO> getBrandsByCountry(String country) {
        return brandRepository.findByCountryAndIsActiveTrueOrderByNameAsc(country).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private String generateSlug(String name) {
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "d")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String originalSlug = slug;
        int counter = 1;
        while (brandRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    private BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setBrandId(brand.getBrandId());
        dto.setName(brand.getName());
        dto.setSlug(brand.getSlug());
        dto.setCountry(brand.getCountry());
        dto.setLogoUrl(brand.getLogoUrl());
        dto.setDescription(brand.getDescription());
        dto.setWebsite(brand.getWebsite());
        dto.setIsActive(brand.getIsActive());
        dto.setCreatedAt(brand.getCreatedAt());

        // Đếm số sản phẩm
        dto.setProductCount(brandRepository.countProductsByBrand(brand.getBrandId()));

        return dto;
    }
}