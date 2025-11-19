package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.MedicalDeviceDTO;
import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.service.BrandService;
import com.example.do_an_tot_nghiep.service.CategoryService;
import com.example.do_an_tot_nghiep.service.MedicalDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final MedicalDeviceService deviceService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String status,
            Model model) {

        // Tạo Pageable với sort by createdAt desc
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy danh sách sản phẩm theo filter
        Page<MedicalDeviceDTO> productsPage;

        // Logic filter
        if (search != null && !search.trim().isEmpty()) {
            productsPage = deviceService.searchProducts(search, pageable);
        } else if (categoryId != null) {
            productsPage = deviceService.getProductsByCategory(categoryId, pageable);
        } else if (brandId != null) {
            productsPage = deviceService.getProductsByBrand(brandId, pageable);
        } else if (status != null && !status.isEmpty()) {
            MedicalDevice.DeviceStatus deviceStatus = MedicalDevice.DeviceStatus.valueOf(status);
            productsPage = deviceService.getProductsByStatus(deviceStatus, pageable);
        } else {
            productsPage = deviceService.getAllProducts(pageable);
        }

        // Lấy danh sách categories và brands cho filter dropdown
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());

        // Add data to model
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("currentPage", productsPage.getNumber());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalItems", productsPage.getTotalElements());

        // Giữ lại filter parameters
        model.addAttribute("searchKeyword", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("status", status);

        return "product/product-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new MedicalDeviceDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("isEdit", false);
        return "product/product-add";
    }

    // THÊM METHOD NÀY - XỬ LÝ THÊM SẢN PHẨM MỚI
    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") MedicalDeviceDTO product,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validate ảnh đại diện
            if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng tải lên ảnh đại diện!");
                redirectAttributes.addFlashAttribute("product", product);
                return "redirect:/admin/products/add";
            }

            // Tạo sản phẩm mới
            deviceService.createProduct(product);

            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
            return "redirect:/admin/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("product", product);
            return "redirect:/admin/products/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        MedicalDeviceDTO product = deviceService.getDeviceById(id);
        if (product == null) {
            return "redirect:/admin/products?error=notfound";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("isEdit", true);
        return "product/product-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable String id,
                                @ModelAttribute("product") MedicalDeviceDTO product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            deviceService.updateProduct(id, product, imageFile);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            deviceService.deleteProduct(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<?> deleteBatch(@RequestBody List<String> ids) {
        try {
            int deletedCount = deviceService.deleteProducts(ids);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deletedCount", deletedCount);
            response.put("message", "Đã xóa " + deletedCount + " sản phẩm");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}