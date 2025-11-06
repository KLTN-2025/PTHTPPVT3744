package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/multiple")
    public List<String> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        try {
            return cloudinaryService.uploadMultipleFiles(files);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}
