package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IMedicalDeviceRepository medicalDeviceRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IBrandRepository brandRepository;

    /**
     * Hiển thị trang danh sách sản phẩm với bộ lọc
     */
    @GetMapping({"", "/"})
    public String products(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {
        try {
            List<MedicalDevice> products = new java.util.ArrayList<>();

            // Lấy sản phẩm theo tiêu chí tìm kiếm (FIX: Logic rõ ràng hơn)
            if (!keyword.isEmpty() && categoryId != null) {
                // Cả keyword VÀ category
                products = medicalDeviceRepository.searchByKeyword(keyword).stream()
                        .filter(p -> p.getCategory() != null &&
                                p.getCategory().getCategoryId().equals(categoryId.longValue()))
                        .collect(Collectors.toList());
            } else if (!keyword.isEmpty()) {
                // Chỉ keyword
                products = medicalDeviceRepository.searchByKeyword(keyword);
            } else if (categoryId != null) {
                // Chỉ category
                products = medicalDeviceRepository.findByCategoryId(categoryId.longValue());
            } else {
                // Không có filter, lấy tất cả
                products = medicalDeviceRepository.findAllActive();
            }

            // Lọc theo thương hiệu (nếu có)
            if (brandId != null) {
                products = products.stream()
                        .filter(p -> p.getBrand() != null && p.getBrand().getBrandId().equals(brandId.longValue()))
                        .collect(Collectors.toList());
            }

            // Sắp xếp
            products = sortProducts(products, sortBy);

            // Phân trang (12 sản phẩm/trang)
            int pageSize = 12;
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, products.size());
            List<MedicalDevice> pageProducts = products.subList(start, end);

            // Tính số trang
            int totalPages = (int) Math.ceil((double) products.size() / pageSize);

            // Lấy danh sách danh mục và thương hiệu để hiển thị bộ lọc
            List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrder();
            List<Brand> brands = brandRepository.findByIsActiveTrue();

            // Thêm dữ liệu vào model
            model.addAttribute("products", pageProducts);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("totalProducts", products.size());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedBrandId", brandId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("title", "Thiết bị y tế - Vật Tư Y Tế ABC");

            System.out.println("Products loaded: " + pageProducts.size());
            System.out.println("Total pages: " + totalPages);

        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("brands", List.of());
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải sản phẩm!");
        }

        return "product/products";
    }

    /**
     * Hiển thị chi tiết sản phẩm
     */
    @GetMapping("/{deviceId}")
    public String productDetail(@PathVariable String deviceId, Model model) {
        try {
            MedicalDevice product = medicalDeviceRepository.findById(deviceId).orElse(null);

            if (product == null) {
                model.addAttribute("errorMessage", "Sản phẩm không tồn tại!");
                return "error/404";
            }

            // Cập nhật lượt xem
            if (product.getViewCount() == null) {
                product.setViewCount(0);
            }
            product.setViewCount(product.getViewCount() + 1);
            medicalDeviceRepository.save(product);

            // Lấy sản phẩm liên quan (cùng danh mục)
            List<MedicalDevice> relatedProducts = new java.util.ArrayList<>();
            if (product.getCategory() != null) {
                relatedProducts = medicalDeviceRepository.findByCategoryId(product.getCategory().getCategoryId().longValue())
                        .stream()
                        .filter(p -> !p.getDeviceId().equals(deviceId))
                        .limit(4)
                        .collect(Collectors.toList());
            }

            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("title", product.getName() + " - Vật Tư Y Tế ABC");

        } catch (Exception e) {
            System.err.println("Error loading product detail: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra!");
            return "error/500";
        }

        return "product/product-detail";
    }

    /**
     * Sắp xếp sản phẩm
     */
    private List<MedicalDevice> sortProducts(List<MedicalDevice> products, String sortBy) {
        switch (sortBy) {
            case "price-asc":
                return products.stream()
                        .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                        .collect(Collectors.toList());
            case "price-desc":
                return products.stream()
                        .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                        .collect(Collectors.toList());
            case "popular":
                return products.stream()
                        .sorted((a, b) -> Integer.compare(
                                b.getViewCount() != null ? b.getViewCount() : 0,
                                a.getViewCount() != null ? a.getViewCount() : 0))
                        .collect(Collectors.toList());
            case "sold":
                return products.stream()
                        .sorted((a, b) -> Integer.compare(
                                b.getSoldCount() != null ? b.getSoldCount() : 0,
                                a.getSoldCount() != null ? a.getSoldCount() : 0))
                        .collect(Collectors.toList());
            case "newest":
            default:
                return products.stream()
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .collect(Collectors.toList());
        }
    }
}