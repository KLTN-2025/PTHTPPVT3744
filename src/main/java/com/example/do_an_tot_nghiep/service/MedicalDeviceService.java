package com.example.do_an_tot_nghiep.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
    private Cloudinary cloudinary;

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
        deviceRepository.delete(device);
    }

    @Transactional
    @Override
    public void updateProduct(String id, MedicalDeviceDTO dto, MultipartFile imageFile) {
        // 1️⃣ Lấy sản phẩm cũ
        MedicalDevice existing = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // 2️⃣ Cập nhật các trường cơ bản
        existing.setName(dto.getName());
        existing.setSku(dto.getSku());
        existing.setPrice(dto.getPrice());
        existing.setOriginalPrice(dto.getOriginalPrice());
        existing.setDiscountPercent(dto.getDiscountPercent());
        existing.setStockQuantity(dto.getStockQuantity());
        existing.setMinStockLevel(dto.getMinStockLevel());
        existing.setStatus(MedicalDevice.DeviceStatus.valueOf(dto.getStatus()));
        existing.setIsFeatured(dto.getIsFeatured());
        existing.setIsNew(dto.getIsNew());
        existing.setDescription(dto.getDescription());

        // 3️⃣ Cập nhật Category & Brand
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu ID: " + dto.getBrandId()));
            existing.setBrand(brand);
        }

        // 4️⃣ Upload ảnh mới nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("folder", "medical_devices/" + id));
                String imageUrl = uploadResult.get("url").toString();
                existing.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage(), e);
            }
        }

        // 5️⃣ Lưu lại
        deviceRepository.save(existing);
    }

    @Transactional
    public int deleteProducts(List<String> ids) {
        int count = 0;
        for (String id : ids) {
            try {
                deviceRepository.deleteById(id);
                count++;
            } catch (Exception e) {
                System.err.println("Không thể xóa sản phẩm ID: " + id + " - " + e.getMessage());
            }
        }
        return count;
    }

    public MedicalDevice uploadAndSaveGallery(String deviceId, MultipartFile[] files) throws IOException {
        MedicalDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + deviceId));

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "medical_devices/" + deviceId));
            urls.add(uploadResult.get("url").toString());
        }

        device.setGalleryUrlList(urls);
        return deviceRepository.save(device);
    }

    @Override
    public MedicalDeviceDTO convertToDTO(MedicalDevice device) {
        Double avgRating = reviewRepository.getAverageRatingByDevice(device);
        Long reviewCount = reviewRepository.countApprovedReviewsByDevice(device);

        return MedicalDeviceDTO.builder()
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .sku(device.getSku())
                .categoryId(device.getCategory() != null ? device.getCategory().getCategoryId() : null)
                .brandId(device.getBrand() != null ? device.getBrand().getBrandId() : null)
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .brandName(device.getBrand() != null ? device.getBrand().getName() : null)
                .supplierName(device.getSupplier() != null ? device.getSupplier().getName() : null)
                .price(device.getPrice())
                .originalPrice(device.getOriginalPrice())
                .discountPercent(device.getDiscountPercent())
                .stockQuantity(device.getStockQuantity())
                .minStockLevel(device.getMinStockLevel())
                .status(device.getStatus() != null ? device.getStatus().name() : null)
                .imageUrl(device.getImageUrl())
                .viewCount(device.getViewCount())
                .soldCount(device.getSoldCount())
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .isFeatured(device.getIsFeatured())
                .isNew(device.getIsNew())
                .description(device.getDescription())
                .build();
    }
}
