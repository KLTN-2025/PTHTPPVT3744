package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.model.MedicalDevice;
import com.example.do_an_tot_nghiep.repository.IMedicalDeviceRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalDevicePromptService {

    private final IMedicalDeviceRepository deviceRepository;
    private static final String SITE_URL = "http://localhost:8080";

    // Từ khóa cho các nhóm sản phẩm
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "huyết áp", List.of("huyết áp", "máy đo", "blood pressure", "huyết", "cao huyết áp"),
            "nhiệt độ", List.of("nhiệt kế", "đo nhiệt", "sốt", "thermometer", "nhiệt độ"),
            "đường huyết", List.of("đường huyết", "tiểu đường", "glucose", "blood sugar", "đái tháo đường"),
            "oxy", List.of("oxy", "spo2", "nhịp tim", "oxygen", "bão hòa"),
            "khẩu trang", List.of("khẩu trang", "mask", "y tế", "3d", "kháng khuẩn")
    );

    public String buildPrompt(String userMessage) {
        try {
            // 1. Phân tích ý định người dùng
            String intent = analyzeIntent(userMessage);

            // 2. Trích xuất từ khóa
            List<String> keywords = extractKeywords(userMessage);

            // 3. Tìm sản phẩm phù hợp
            List<MedicalDevice> relevantDevices = findRelevantDevices(keywords, userMessage);

            // 4. Xây dựng context
            return buildContext(intent, relevantDevices, userMessage);

        } catch (Exception e) {
            log.error("Error building prompt", e);
            return "Không tìm thấy thông tin sản phẩm phù hợp.";
        }
    }

    private String analyzeIntent(String message) {
        String lower = message.toLowerCase();

        if (lower.matches(".*(bao nhiêu|giá|price|cost|chi phí).*")) {
            return "PRICE_INQUIRY";
        } else if (lower.matches(".*(so sánh|compare|khác nhau|nên chọn).*")) {
            return "COMPARISON";
        } else if (lower.matches(".*(cách dùng|sử dụng|how to|hướng dẫn).*")) {
            return "USAGE_GUIDE";
        } else if (lower.matches(".*(mua|order|đặt|buy).*")) {
            return "PURCHASE";
        } else if (lower.matches(".*(tốt|recommend|đề xuất|nên|gợi ý).*")) {
            return "RECOMMENDATION";
        } else if (lower.matches(".*(còn hàng|available|có sẵn|stock).*")) {
            return "AVAILABILITY";
        } else {
            return "GENERAL_INQUIRY";
        }
    }

    private List<String> extractKeywords(String message) {
        List<String> keywords = new ArrayList<>();
        String lower = message.toLowerCase();

        // Tìm từ khóa category
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    keywords.add(entry.getKey());
                    break;
                }
            }
        }

        // Tìm thương hiệu
        List<String> brands = List.of("omron", "beurer", "microlife", "rossmax", "citizen");
        for (String brand : brands) {
            if (lower.contains(brand)) {
                keywords.add(brand);
            }
        }

        // Tìm mức giá
        if (lower.matches(".*(rẻ|giá rẻ|cheap|under|dưới).*")) {
            keywords.add("budget");
        } else if (lower.matches(".*(cao cấp|premium|chất lượng cao).*")) {
            keywords.add("premium");
        }

        return keywords;
    }

    private List<MedicalDevice> findRelevantDevices(List<String> keywords, String message) {
        List<MedicalDevice> devices;

        if (keywords.isEmpty()) {
            // Không có từ khóa cụ thể -> lấy sản phẩm nổi bật
            devices = deviceRepository.findTop10ByStatusOrderBySoldCountDesc(
                    MedicalDevice.DeviceStatus.Còn_hàng
            );
        } else {
            // Tìm theo từ khóa
            String searchTerm = String.join(" ", keywords);
            devices = deviceRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    searchTerm, searchTerm
            );

            // Nếu không tìm thấy, search rộng hơn
            if (devices.isEmpty()) {
                devices = deviceRepository.findTop20ByStatusOrderByViewCountDesc(
                        MedicalDevice.DeviceStatus.Còn_hàng
                );
            }
        }

        // Sắp xếp và lọc theo tiêu chí
        return devices.stream()
                .sorted(this::compareDevices)
                .limit(5) // Top 5 sản phẩm phù hợp nhất
                .collect(Collectors.toList());
    }

    private int compareDevices(MedicalDevice d1, MedicalDevice d2) {
        // Ưu tiên: Còn hàng > Rating > Sold count > Featured
        int score1 = calculateDeviceScore(d1);
        int score2 = calculateDeviceScore(d2);
        return Integer.compare(score2, score1); // Giảm dần
    }

    private int calculateDeviceScore(MedicalDevice device) {
        int score = 0;

        // Còn hàng: +1000
        if (device.getStatus() == MedicalDevice.DeviceStatus.Còn_hàng) {
            score += 1000;
        }

        // Rating: +100 * rating
        score += (int)(device.getAverageRating() * 100);

        // Sold count: +1 * sold
        score += (device.getSoldCount() != null ? device.getSoldCount() : 0);

        // Featured: +500
        if (Boolean.TRUE.equals(device.getIsFeatured())) {
            score += 500;
        }

        // New: +200
        if (Boolean.TRUE.equals(device.getIsNew())) {
            score += 200;
        }

        return score;
    }

    private String buildContext(String intent, List<MedicalDevice> devices, String message) {
        if (devices.isEmpty()) {
            return buildNoProductContext(message);
        }

        StringBuilder context = new StringBuilder();
        context.append("=== THÔNG TIN SẢN PHẨM LIÊN QUAN ===\n\n");
        context.append("Ý định khách hàng: ").append(getIntentDescription(intent)).append("\n\n");

        for (int i = 0; i < devices.size(); i++) {
            MedicalDevice device = devices.get(i);
            context.append(String.format("## SẢN PHẨM %d:\n", i + 1));
            context.append(formatDeviceInfo(device));
            context.append("\n---\n\n");
        }

        context.append(buildRecommendationGuide(intent));

        return context.toString();
    }

    private String formatDeviceInfo(MedicalDevice device) {
        return String.format("""
    **Tên**: %s
    **Mã**: %s
    **Giá**: %s đ %s
    **Thương hiệu**: %s
    **Danh mục**: %s
    **Tình trạng**: %s (Còn: %d %s)
    **Đánh giá**: %.1f⭐ (%d lượt)
    **Đã bán**: %d sản phẩm
    **Bảo hành**: %s
    
    **Mô tả**: %s
    
    **Thông số kỹ thuật**: %s
    
    **Hướng dẫn sử dụng**: %s
    
    **Link sản phẩm**: %s/products/%s
    **Hình ảnh**: %s
    %s
    """,
                device.getName(),
                device.getSku(),
                formatPrice(device.getPrice()),
                device.getDiscountPercent() > 0 ?
                        String.format("(Giảm %d%% từ %s đ)",
                                device.getDiscountPercent(),
                                formatPrice(device.getOriginalPrice()))
                        : "",
                device.getBrand() != null ? device.getBrand().getName() : "N/A",
                device.getCategory() != null ? device.getCategory().getName() : "N/A",
                device.getStatus().getDisplayName(),
                device.getStockQuantity(),
                device.getUnit(),
                device.getAverageRating(),
                device.getTotalReviews(),
                device.getSoldCount() != null ? device.getSoldCount() : 0,
                device.getWarrantyPeriod() != null ? device.getWarrantyPeriod() + " tháng" : "Không có",
                truncate(device.getDescription(), 200),
                truncate(device.getSpecification(), 200),
                truncate(device.getUsageInstruction(), 150),
                SITE_URL,
                device.getDeviceId(),
                device.getImageUrl() != null ? device.getImageUrl() : "N/A",
                device.getGalleryUrls() != null && !device.getGalleryUrls().isEmpty()
                        ? "\n**Thư viện ảnh**: " +
                        String.join(", ", device.getGalleryUrlList().stream().limit(3).toList())
                        : ""
        );

    }

    private String getIntentDescription(String intent) {
        return switch (intent) {
            case "PRICE_INQUIRY" -> "Hỏi về giá";
            case "COMPARISON" -> "So sánh sản phẩm";
            case "USAGE_GUIDE" -> "Hỏi cách sử dụng";
            case "PURCHASE" -> "Muốn mua hàng";
            case "RECOMMENDATION" -> "Xin tư vấn sản phẩm";
            case "AVAILABILITY" -> "Kiểm tra còn hàng";
            default -> "Hỏi thông tin chung";
        };
    }

    private String buildRecommendationGuide(String intent) {
        return switch (intent) {
            case "PRICE_INQUIRY" -> """
                💡 GỢI Ý TƯ VẤN:
                - So sánh giá các sản phẩm
                - Giải thích giá trị đồng tiền
                - Đề xuất sản phẩm phù hợp ngân sách
                """;
            case "COMPARISON" -> """
                💡 GỢI Ý TƯ VẤN:
                - So sánh chi tiết ưu/nhược điểm
                - Phân tích phù hợp với nhu cầu nào
                - Đề xuất sản phẩm tốt nhất
                """;
            case "PURCHASE" -> """
                💡 GỢI Ý TƯ VẤN:
                - Xác nhận sản phẩm phù hợp
                - Hướng dẫn đặt hàng
                - Thông tin giao hàng & thanh toán
                """;
            default -> """
                💡 GỢI Ý TƯ VẤN:
                - Giới thiệu sản phẩm nổi bật
                - Hỏi thêm nhu cầu cụ thể
                - Tư vấn phù hợp nhất
                """;
        };
    }

    private String buildNoProductContext(String message) {
        return String.format("""
            === KHÔNG TÌM THẤY SẢN PHẨM PHÙ HỢP ===
            
            Tin nhắn: %s
            
            💡 GỢI Ý XỬ LÝ:
            - Xin lỗi khách lịch sự
            - Hỏi thêm thông tin chi tiết
            - Đề xuất các sản phẩm tương tự
            - Đăng ký thông báo khi có hàng
            - Gợi ý liên hệ trực tiếp để được tư vấn
            """, message);
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,d", price.longValue());
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) return "Chưa có thông tin";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}