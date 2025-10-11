package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.MedicalDeviceDTO;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.repository.IMedicalDeviceRepository;
import com.example.do_an_tot_nghiep.repository.IReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalDeviceService implements IMedicalDeviceService {
    @Autowired
    private IMedicalDeviceRepository deviceRepository;
    @Autowired
    private IReviewRepository reviewRepository;

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
    public MedicalDeviceDTO convertToDTO(MedicalDevice device) {
        Double avgRating = reviewRepository.getAverageRatingByDevice(device);
        Long reviewCount = reviewRepository.countApprovedReviewsByDevice(device);

        return MedicalDeviceDTO.builder()
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .sku(device.getSku())
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .brandName(device.getBrand() != null ? device.getBrand().getName() : null)
                .supplierName(device.getSupplier() != null ? device.getSupplier().getName() : null)
                .price(device.getPrice())
                .originalPrice(device.getOriginalPrice())
                .discountPercent(device.getDiscountPercent())
                .stockQuantity(device.getStockQuantity())
                .minStockLevel(device.getMinStockLevel())
                .status(String.valueOf(device.getStatus()))
                .imageUrl(device.getImageUrl())
                .viewCount(device.getViewCount())
                .soldCount(device.getSoldCount())
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .isFeatured(device.getIsFeatured())
                .isNew(device.getIsNew())
                .build();
    }
}
