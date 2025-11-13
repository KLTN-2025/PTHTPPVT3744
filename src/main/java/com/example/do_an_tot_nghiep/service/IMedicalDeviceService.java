package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.MedicalDeviceDTO;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMedicalDeviceService {
    List<MedicalDeviceDTO> getLowStockProducts();

    MedicalDeviceDTO getDeviceById(String deviceId);

    MedicalDeviceDTO convertToDTO(MedicalDevice device);

    Page<MedicalDeviceDTO> getAllProducts(Pageable pageable);

    Page<MedicalDeviceDTO> getProductsByStatus(MedicalDevice.DeviceStatus status, Pageable pageable);

    Page<MedicalDeviceDTO> getProductsByBrand(Integer brandId, Pageable pageable);

    Page<MedicalDeviceDTO> getProductsByCategory(Integer categoryId, Pageable pageable);

    Page<MedicalDeviceDTO> searchProducts(String keyword, Pageable pageable);

    void deleteProduct(String id);

    void updateProduct(String id, MedicalDeviceDTO product, MultipartFile imageFile);
}
