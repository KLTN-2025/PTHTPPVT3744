package com.example.do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Database câu hỏi thường gặp (Rule-based)
    private final Map<String, String> faqDatabase = new HashMap<>() {{
        // Về sản phẩm
        put("giá|giá cả|bao nhiêu tiền",
                "Giá sản phẩm dao động từ 100.000đ - 5.000.000đ tùy loại. Bạn có thể xem giá chi tiết tại trang danh mục sản phẩm.");

        put("chất lượng|chính hãng|hàng thật",
                "Chúng tôi cam kết 100% sản phẩm chính hãng, có tem nhãn đầy đủ. Bảo hành theo chính sách nhà sản xuất.");

        put("bảo hành|warranty",
                "Sản phẩm được bảo hành 6-12 tháng tùy loại. Bảo hành 1 đổi 1 trong 30 ngày đầu nếu lỗi nhà sản xuất.");

        // Về giao hàng
        put("giao hàng|ship|vận chuyển|delivery",
                "📦 Giao hàng toàn quốc:\n- Nội thành: 1-2 ngày\n- Tỉnh xa: 3-5 ngày\n- Miễn phí ship đơn > 500k");

        put("phí ship|phí giao hàng",
                "Phí ship 30.000đ. MIỄN PHÍ cho đơn hàng trên 500.000đ!");


        // Về thanh toán
        put("thanh toán|payment|pay|vnpay|momo",
                "💳 Chúng tôi hỗ trợ:\n- VNPAY (ATM/Visa/Master)\n- MoMo\n- COD (Thanh toán khi nhận hàng)\n- Chuyển khoản ngân hàng");

        put("cod|tiền mặt",
                "Có hỗ trợ COD (thanh toán khi nhận hàng) cho tất cả đơn hàng!");

        // Về đơn hàng
        put("kiểm tra đơn|tra đơn|đơn hàng",
                "Bạn có thể kiểm tra đơn hàng tại mục 'Đơn hàng của tôi' sau khi đăng nhập.");

        put("hủy đơn|cancel",
                "Bạn có thể hủy đơn hàng trong vòng 24h sau khi đặt. Truy cập 'Đơn hàng của tôi' > Chọn đơn cần hủy.");

        put("hoàn tiền|refund",
                "Hoàn tiền trong 5-7 ngày làm việc sau khi xác nhận hủy/trả hàng thành công.");

        // Về khuyến mãi
        put("khuyến mãi|giảm giá|sale|voucher|mã giảm",
                "🎁 Khuyến mãi hiện tại:\n- Giảm 10% đơn đầu tiên\n- Freeship đơn > 500k\n- Tích điểm đổi quà\nMã: WELCOME10");

        // Về tài khoản
        put("đăng ký|tạo tài khoản|register",
                "Click 'Đăng ký' góc trên bên phải, điền thông tin email và mật khẩu. Xác nhận email để kích hoạt tài khoản!");

        put("quên mật khẩu|reset password",
                "Click 'Quên mật khẩu' ở trang đăng nhập. Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu.");

        // Về liên hệ
        put("liên hệ|hotline|email|contact",
                "📞 Liên hệ:\n- Hotline: 1900-xxxx\n- Email: support@shop.com\n- Giờ làm việc: 8h-22h hàng ngày");

        // Chào hỏi
        put("xin chào|hello|hi|chào",
                "Xin chào! 👋 Tôi là trợ lý ảo của shop. Tôi có thể giúp gì cho bạn?");

        put("cảm ơn|thank",
                "Rất vui được hỗ trợ bạn! 😊 Nếu cần thêm thông tin gì, đừng ngại hỏi nhé!");
    }};

    /**
     * Xử lý tin nhắn từ người dùng
     */
    public String getResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Bạn cần hỗ trợ gì ạ? 😊";
        }

        userMessage = userMessage.toLowerCase().trim();

        // Kiểm tra có dùng OpenAI không
        if (openaiApiKey != null && !openaiApiKey.isEmpty() && !openaiApiKey.equals("your-api-key-here")) {
            try {
                return getOpenAIResponse(userMessage);
            } catch (Exception e) {
                System.err.println("OpenAI Error: " + e.getMessage());
                // Fallback sang rule-based nếu OpenAI lỗi
            }
        }

        // Rule-based chatbot (Miễn phí)
        return getRuleBasedResponse(userMessage);
    }

    /**
     * Chatbot Rule-based (Miễn phí - Không cần API)
     */
    private String getRuleBasedResponse(String message) {
        // Tìm câu trả lời phù hợp từ database
        for (Map.Entry<String, String> entry : faqDatabase.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (message.contains(keyword.trim())) {
                    return entry.getValue();
                }
            }
        }

        // Câu trả lời mặc định
        return "Xin lỗi, tôi chưa hiểu câu hỏi của bạn. 😅\n\n" +
                "Bạn có thể hỏi tôi về:\n" +
                "• Giá sản phẩm\n" +
                "• Giao hàng\n" +
                "• Thanh toán\n" +
                "• Khuyến mãi\n" +
                "• Chính sách bảo hành\n\n" +
                "Hoặc liên hệ hotline: 1900-xxxx";
    }

    /**
     * Chatbot OpenAI (Thông minh - Cần API key)
     */
    private String getOpenAIResponse(String message) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";

            // Tạo request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            // Tạo messages array
            ArrayNode messages = requestBody.putArray("messages");

            // System message (Vai trò của AI)
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "Bạn là trợ lý ảo của shop bán hàng online. " +
                            "Hãy trả lời ngắn gọn, thân thiện bằng tiếng Việt. " +
                            "Thông tin shop: Giao hàng 1-2 ngày nội thành, thanh toán VNPAY/MoMo/COD, " +
                            "bảo hành 6-12 tháng, khuyến mãi giảm 10% đơn đầu với mã WELCOME10."
            );

            // User message
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", message);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            // Gọi API
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response
            ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(response.getBody());
            return jsonResponse.get("choices").get(0)
                    .get("message").get("content").asText();

        } catch (Exception e) {
            throw new RuntimeException("OpenAI API Error: " + e.getMessage());
        }
    }
}