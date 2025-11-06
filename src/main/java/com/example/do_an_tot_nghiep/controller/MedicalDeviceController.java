package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.service.MedicalDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/device")
public class MedicalDeviceController {

    @Autowired
    private MedicalDeviceService medicalDeviceService;

    @PostMapping("/{deviceId}/upload-gallery")
    public MedicalDevice uploadGallery(
            @PathVariable String deviceId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            return medicalDeviceService.uploadAndSaveGallery(deviceId, files);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
        }
    }
}
