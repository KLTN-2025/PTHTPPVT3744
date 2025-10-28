package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.model.*;
import com.example.do_an_tot_nghiep.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("")
public class HomeController {

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IMedicalDeviceRepository medicalDeviceRepository;

    @Autowired
    private IPromotionRepository promotionRepository;

    @Autowired
    private IBannerRepository bannerRepository;

    @Autowired
    private IBlogPostRepository blogPostRepository;

    @GetMapping({"", "/", "/home"})
    public String home(Model model) {
        try {
            // Thêm title cho trang
            model.addAttribute("title", "Trang chủ - Vật Tư Y Tế ABC");

            // Lấy 6 danh mục đang hoạt động
            List<Category> categories = categoryRepository.findTop6ByIsActiveTrueOrderByDisplayOrder();

            // Lấy 8 sản phẩm nổi bật
            List<MedicalDevice> featuredProducts = medicalDeviceRepository.findFeaturedProducts();

            // Lấy 4 sản phẩm mới
            List<MedicalDevice> newProducts = medicalDeviceRepository.findTop4NewProducts();

            // Lấy khuyến mãi đang hoạt động
            LocalDateTime now = LocalDateTime.now();
            List<Promotion> promotions = promotionRepository.findActivePromotions(now);

            // Lấy banner đang hoạt động
            List<Banner> banners = bannerRepository.findActiveBanners(now);

            // Lấy 3 bài viết blog mới nhất
            List<BlogPost> blogPosts = blogPostRepository.findTop3PublishedPosts();

            // Thêm vào model
            model.addAttribute("categories", categories);
            model.addAttribute("featuredProducts", featuredProducts);
            model.addAttribute("newProducts", newProducts);
            model.addAttribute("promotions", promotions);
            model.addAttribute("banners", banners);
            model.addAttribute("blogPosts", blogPosts);

            // Log để debug (có thể bỏ sau khi test xong)
            System.out.println("Categories loaded: " + categories.size());
            System.out.println("Featured products loaded: " + featuredProducts.size());
            System.out.println("New products loaded: " + newProducts.size());
            System.out.println("Promotions loaded: " + promotions.size());
            System.out.println("Banners loaded: " + banners.size());
            System.out.println("Blog posts loaded: " + blogPosts.size());

        } catch (Exception e) {
            // Log lỗi chi tiết
            System.err.println("Error loading home page data: " + e.getMessage());
            e.printStackTrace();

            // Thêm dữ liệu rỗng để tránh lỗi template
            model.addAttribute("categories", List.of());
            model.addAttribute("featuredProducts", List.of());
            model.addAttribute("newProducts", List.of());
            model.addAttribute("promotions", List.of());
            model.addAttribute("banners", List.of());
            model.addAttribute("blogPosts", List.of());

            // Thêm message lỗi để hiển thị cho user (optional)
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại sau!");
        }

        return "home/home";
    }
}