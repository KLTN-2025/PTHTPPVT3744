package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.MedicalDeviceDTO;
import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalDeviceService implements IMedicalDeviceService {

    @Autowired
    private IMedicalDeviceRepository deviceRepository;
    @Autowired
    private IReviewRepository reviewRepository;
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private IBrandRepository brandRepository;
    @Autowired
    private ISupplierRepository supplierRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<MedicalDeviceDTO> getLowStockProducts() {
        return deviceRepository.findLowStockProducts()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MedicalDeviceDTO getDeviceById(String deviceId) {
        MedicalDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return convertToDTO(device);
    }

    @Override
    public Page<MedicalDeviceDTO> getAllProducts(Pageable pageable) {
        return deviceRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public Page<MedicalDeviceDTO> getProductsByStatus(MedicalDevice.DeviceStatus status, Pageable pageable) {
        return deviceRepository.findByStatus(status, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<MedicalDeviceDTO> getProductsByBrand(Integer brandId, Pageable pageable) {
        return deviceRepository.findByBrandBrandId(brandId, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<MedicalDeviceDTO> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return deviceRepository.findByCategoryCategoryId(categoryId, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<MedicalDeviceDTO> searchProducts(String keyword, Pageable pageable) {
        return deviceRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public void deleteProduct(String id) {
        MedicalDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Xóa ảnh chính trên Cloudinary
        if (device.getImagePublicId() != null && !device.getImagePublicId().isEmpty()) {
            cloudinaryService.delete(device.getImagePublicId());
        }

        // Xóa gallery trên Cloudinary
        List<String> galleryUrls = device.getGalleryUrlList();
        if (galleryUrls != null && !galleryUrls.isEmpty()) {
            for (String url : galleryUrls) {
                // Extract publicId from URL if needed
                String publicId = extractPublicIdFromUrl(url);
                if (publicId != null) {
                    cloudinaryService.delete(publicId);
                }
            }
        }

        deviceRepository.delete(device);
    }

    @Transactional
    @Override
    public void updateProduct(String id, MedicalDeviceDTO dto, MultipartFile imageFile) throws IOException {
        MedicalDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        device.setName(dto.getName());
        device.setSku(dto.getSku());
        device.setPrice(dto.getPrice() != null ? BigDecimal.valueOf(dto.getPrice()) : null);
        device.setStockQuantity(dto.getStockQuantity());
        device.setStatus(dto.getStatus());
        device.setIsFeatured(dto.getIsFeatured());
        device.setIsNew(dto.getIsNew());

        // Category & Brand
        if (dto.getCategoryId() != null) {
            device.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
        if (dto.getBrandId() != null) {
            device.setBrand(brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found")));
        }

        // Upload ảnh chính
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ
            if (device.getImagePublicId() != null) {
                cloudinaryService.delete(device.getImagePublicId());
            }

            Map<String, String> uploaded = cloudinaryService.uploadFile(imageFile, "medical_devices/" + id, "main");
            device.setImageUrl(uploaded.get("url"));
            device.setImagePublicId(uploaded.get("publicId"));
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty()) {
            device.setImageUrl(dto.getImageUrl());
        }

        // Update gallery
        if (dto.getGalleryFiles() != null && !dto.getGalleryFiles().isEmpty()) {
            // Xóa gallery cũ trên Cloudinary
            List<String> oldGalleryUrls = device.getGalleryUrlList();
            if (oldGalleryUrls != null && !oldGalleryUrls.isEmpty()) {
                for (String url : oldGalleryUrls) {
                    String publicId = extractPublicIdFromUrl(url);
                    if (publicId != null) {
                        cloudinaryService.delete(publicId);
                    }
                }
            }

            // Upload gallery mới
            List<String> newGalleryUrls = new ArrayList<>();
            for (MultipartFile file : dto.getGalleryFiles()) {
                Map<String, String> uploaded = cloudinaryService.uploadFile(file, "medical_devices/" + id, "gallery");
                newGalleryUrls.add(uploaded.get("url"));
            }
            device.setGalleryUrlList(newGalleryUrls);
        }

        deviceRepository.save(device);
    }

    @Transactional
    @Override
    public void createProduct(MedicalDeviceDTO productDTO) throws IOException {

        // === 1. KHỞI TẠO ENTITY ===
        MedicalDevice device = new MedicalDevice();

        // Generate ID & SKU
        String deviceId = productDTO.getDeviceId() != null
                ? productDTO.getDeviceId()
                : generateDeviceId();

        device.setDeviceId(deviceId);
        device.setSku(productDTO.getSku() != null ? productDTO.getSku() : generateSKU());
        device.setSlug(generateSlug(productDTO.getName()));

        // === 2. THÔNG TIN CHÍNH ===
        device.setName(productDTO.getName());
        device.setDescription(productDTO.getDescription());
        device.setSpecification(productDTO.getSpecification());
        device.setUsageInstruction(productDTO.getUsageInstruction());

        device.setPrice(productDTO.getPrice() != null ? BigDecimal.valueOf(productDTO.getPrice()) : BigDecimal.ZERO);
        device.setOriginalPrice(productDTO.getOriginalPrice() != null ? BigDecimal.valueOf(productDTO.getOriginalPrice()) : null);
        device.setDiscountPercent(productDTO.getDiscountPercent() != null ? productDTO.getDiscountPercent() : 0);

        device.setStockQuantity(Optional.ofNullable(productDTO.getStockQuantity()).orElse(0));
        device.setMinStockLevel(Optional.ofNullable(productDTO.getMinStockLevel()).orElse(10));
        device.setUnit(Optional.ofNullable(productDTO.getUnit()).orElse("Cái"));
        device.setWeight(productDTO.getWeight() != null ? BigDecimal.valueOf(productDTO.getWeight()) : null);

        device.setDimensions(productDTO.getDimensions());
        device.setWarrantyPeriod(productDTO.getWarrantyPeriod());

        device.setStatus(
                productDTO.getStatus() != null ?
                        productDTO.getStatus() :
                        MedicalDevice.DeviceStatus.Còn_hàng
        );

        device.setIsFeatured(Optional.ofNullable(productDTO.getIsFeatured()).orElse(false));
        device.setIsNew(Optional.ofNullable(productDTO.getIsNew()).orElse(false));

        // === 3. CATEGORY - BRAND - SUPPLIER ===
        if (productDTO.getCategoryId() != null) {
            device.setCategory(categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }
        if (productDTO.getBrandId() != null) {
            device.setBrand(brandRepository.findById(productDTO.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found")));
        }
        if (productDTO.getSupplierId() != null) {
            device.setSupplier(supplierRepository.findById(productDTO.getSupplierId()).orElse(null));
        }

        // === 4. UPLOAD ẢNH CHÍNH ===
        if (productDTO.getImageFile() != null && !productDTO.getImageFile().isEmpty()) {

            Map<String, String> uploaded = cloudinaryService.uploadFile(
                    productDTO.getImageFile(),
                    "medical_devices/" + deviceId,
                    "main"
            );

            device.setImageUrl(uploaded.get("url"));
            device.setImagePublicId(uploaded.get("publicId"));
        }

        // === 5. UPLOAD GALLERY ===
        if (productDTO.getGalleryFiles() != null && !productDTO.getGalleryFiles().isEmpty()) {

            List<String> galleryUrls = new ArrayList<>();

            for (MultipartFile file : productDTO.getGalleryFiles()) {

                if (file.isEmpty()) continue;

                Map<String, String> uploaded = cloudinaryService.uploadFile(
                        file,
                        "medical_devices/" + deviceId,
                        "gallery"
                );

                galleryUrls.add(uploaded.get("url"));
            }

            device.setGalleryUrlList(galleryUrls);
        }

        // === 6. KHỞI TẠO GIÁ TRỊ MẶC ĐỊNH KHÁC ===
        device.setViewCount(0);
        device.setSoldCount(0);

        // === 7. LƯU DATABASE ===
        deviceRepository.save(device);
    }


    @Transactional
    public int deleteProducts(List<String> ids) {
        int count = 0;
        for (String id : ids) {
            try {
                deleteProduct(id);
                count++;
            } catch (Exception e) {
                System.err.println("Không thể xóa sản phẩm ID: " + id + " - " + e.getMessage());
            }
        }
        return count;
    }

    // Helper methods
    private String generateDeviceId() {
        return "DEV" + System.currentTimeMillis();
    }

    private String generateSKU() {
        return "SKU" + System.currentTimeMillis();
    }

    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) return "";
        String slug = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug;
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            // Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
            // Extract public_id from URL
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String pathPart = parts[1];
                // Remove version (v1234567890/)
                pathPart = pathPart.replaceFirst("v\\d+/", "");
                // Remove file extension
                int lastDot = pathPart.lastIndexOf('.');
                if (lastDot > 0) {
                    return pathPart.substring(0, lastDot);
                }
                return pathPart;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract publicId from URL: " + url);
        }
        return null;
    }

    @Override
    public MedicalDeviceDTO convertToDTO(MedicalDevice device) {
        Double avgRating = reviewRepository.getAverageRatingByDevice(device);
        Long reviewCount = reviewRepository.countApprovedReviewsByDevice(device);

        return MedicalDeviceDTO.builder()
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .slug(device.getSlug())
                .sku(device.getSku())
                .categoryId(device.getCategory() != null ? device.getCategory().getCategoryId() : null)
                .brandId(device.getBrand() != null ? device.getBrand().getBrandId() : null)
                .supplierId(device.getSupplier() != null ? device.getSupplier().getSupplierId() : null)
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .brandName(device.getBrand() != null ? device.getBrand().getName() : null)
                .supplierName(device.getSupplier() != null ? device.getSupplier().getName() : null)
                .description(device.getDescription())
                .specification(device.getSpecification())
                .usageInstruction(device.getUsageInstruction())
                .price(device.getPrice() != null ? device.getPrice().doubleValue() : null)
                .originalPrice(device.getOriginalPrice() != null ? device.getOriginalPrice().doubleValue() : null)
                .discountPercent(device.getDiscountPercent())
                .stockQuantity(device.getStockQuantity())
                .minStockLevel(device.getMinStockLevel())
                .unit(device.getUnit())
                .weight(device.getWeight() != null ? device.getWeight().doubleValue() : null)
                .dimensions(device.getDimensions())
                .warrantyPeriod(device.getWarrantyPeriod())
                .status(device.getStatus())
                .isFeatured(device.getIsFeatured())
                .isNew(device.getIsNew())
                .viewCount(device.getViewCount())
                .soldCount(device.getSoldCount())
                .imageUrl(device.getImageUrl())
                .imagePublicId(device.getImagePublicId())
                .galleryUrls(device.getGalleryUrlList())
                .metaKeywords(device.getMetaKeywords())
                .metaDescription(device.getMetaDescription())
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}