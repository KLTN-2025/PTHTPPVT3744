package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.MedicalDeviceDTO;
import com.example.do_an_tot_nghiep.model.MedicalDevice;

import java.util.List;

public interface IMedicalDeviceService {
    List<MedicalDeviceDTO> getLowStockProducts();

    MedicalDeviceDTO getDeviceById(String deviceId);

    MedicalDeviceDTO convertToDTO(MedicalDevice device);
}
