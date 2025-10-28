package com.example.do_an_tot_nghiep.service;

import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * ThymeleafHelper - Service hỗ trợ các hàm tiện ích cho Thymeleaf template
 * Sử dụng trong template với cú pháp: ${@helper.methodName(...)}
 */
@Service("helper")
public class ThymeleafHelper {

    /**
     * Format giá tiền theo định dạng Việt Nam
     * Ví dụ: 1000000 -> "1.000.000 đ"
     *
     * @param price Giá cần format
     * @return Chuỗi giá đã format
     */
    public String formatPrice(Double price) {
        if (price == null) {
            return "0 đ";
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " đ";
    }

    /**
     * Tính giá sau khi giảm
     *
     * @param price Giá gốc
     * @param discountPercent Phần trăm giảm giá
     * @return Giá sau giảm
     */
    public Double calculateDiscountPrice(Double price, Double discountPercent) {
        if (price == null) {
            return 0.0;
        }
        if (discountPercent == null || discountPercent == 0) {
            return price;
        }
        return price - (price * discountPercent / 100);
    }

    /**
     * Tính số tiền được giảm
     *
     * @param price Giá gốc
     * @param discountPercent Phần trăm giảm giá
     * @return Số tiền được giảm
     */
    public Double calculateDiscountAmount(Double price, Double discountPercent) {
        if (price == null || discountPercent == null) {
            return 0.0;
        }
        return price * discountPercent / 100;
    }

    /**
     * Tạo chuỗi sao đánh giá (HTML)
     * Ví dụ: rating = 4 -> ★★★★☆
     *
     * @param rating Số sao (0-5)
     * @return Chuỗi HTML các ngôi sao
     */
    public String createStars(Integer rating) {
        if (rating == null || rating < 0 || rating > 5) {
            rating = 0;
        }
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return stars.toString();
    }

    /**
     * Tạo sao với HTML class để style riêng
     *
     * @param rating Số sao
     * @return HTML string
     */
    public String createStarsHtml(Integer rating) {
        if (rating == null || rating < 0 || rating > 5) {
            rating = 0;
        }
        StringBuilder html = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                html.append("<i class='fas fa-star'></i>");
            } else {
                html.append("<i class='far fa-star'></i>");
            }
        }
        return html.toString();
    }

    /**
     * Kiểm tra xem có phải là list rỗng không
     *
     * @param list List cần kiểm tra
     * @return true nếu list null hoặc rỗng
     */
    public boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Lấy size của list
     *
     * @param list List cần đếm
     * @return Số phần tử trong list
     */
    public int size(List<?> list) {
        return list == null ? 0 : list.size();
    }

    /**
     * Cắt chuỗi với độ dài tối đa và thêm "..."
     *
     * @param text Chuỗi cần cắt
     * @param maxLength Độ dài tối đa
     * @return Chuỗi đã cắt
     */
    public String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Cắt chuỗi theo từ (không cắt giữa từ)
     *
     * @param text Chuỗi cần cắt
     * @param maxLength Độ dài tối đa
     * @return Chuỗi đã cắt
     */
    public String truncateWords(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');

        if (lastSpace > 0) {
            truncated = truncated.substring(0, lastSpace);
        }

        return truncated + "...";
    }

    /**
     * Format số lượng review
     *
     * @param count Số lượng review
     * @return Chuỗi format "(count)"
     */
    public String formatReviewCount(Long count) {
        if (count == null || count == 0) {
            return "";
        }
        return "(" + count + ")";
    }

    /**
     * Format ngày tháng theo định dạng Việt Nam
     *
     * @param dateTime LocalDateTime cần format
     * @return Chuỗi ngày đã format
     */
    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

    /**
     * Format ngày giờ đầy đủ
     *
     * @param dateTime LocalDateTime cần format
     * @return Chuỗi ngày giờ đã format
     */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Tính thời gian từ hiện tại (time ago)
     * Ví dụ: "2 giờ trước", "3 ngày trước"
     *
     * @param dateTime Thời điểm cần tính
     * @return Chuỗi thời gian tương đối
     */
    public String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(dateTime, now).getSeconds();

        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " phút trước";
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " giờ trước";
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            return days + " ngày trước";
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + " tháng trước";
        } else {
            long years = seconds / 31536000;
            return years + " năm trước";
        }
    }

    /**
     * Kiểm tra chuỗi có rỗng không
     *
     * @param str Chuỗi cần kiểm tra
     * @return true nếu null hoặc rỗng
     */
    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Tính phần trăm giảm giá từ giá gốc và giá sale
     *
     * @param originalPrice Giá gốc
     * @param salePrice Giá sale
     * @return Phần trăm giảm
     */
    public Integer calculateDiscountPercent(Double originalPrice, Double salePrice) {
        if (originalPrice == null || salePrice == null || originalPrice == 0) {
            return 0;
        }
        double discount = ((originalPrice - salePrice) / originalPrice) * 100;
        return (int) Math.round(discount);
    }

    /**
     * Format số lượng lớn (1K, 1M)
     *
     * @param count Số cần format
     * @return Chuỗi đã format
     */
    public String formatLargeNumber(Long count) {
        if (count == null) {
            return "0";
        }

        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1000000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.format("%.1fM", count / 1000000.0);
        }
    }

    /**
     * Kiểm tra một số có nằm trong khoảng không
     *
     * @param value Giá trị cần kiểm tra
     * @param min Giá trị min
     * @param max Giá trị max
     * @return true nếu nằm trong khoảng
     */
    public boolean isBetween(Double value, Double min, Double max) {
        if (value == null) {
            return false;
        }
        return value >= min && value <= max;
    }

    /**
     * Tạo URL slug từ tiêu đề
     *
     * @param text Tiêu đề
     * @return URL slug
     */
    public String createSlug(String text) {
        if (text == null) {
            return "";
        }

        // Chuyển về chữ thường và loại bỏ dấu
        String slug = text.toLowerCase()
                .replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a")
                .replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e")
                .replaceAll("ì|í|ị|ỉ|ĩ", "i")
                .replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o")
                .replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u")
                .replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "") // Loại bỏ ký tự đặc biệt
                .replaceAll("\\s+", "-") // Thay space bằng -
                .replaceAll("-+", "-") // Loại bỏ - trùng
                .replaceAll("^-|-$", ""); // Loại bỏ - đầu cuối

        return slug;
    }

    /**
     * Kiểm tra trạng thái đơn hàng
     *
     * @param status Trạng thái
     * @return Class CSS tương ứng
     */
    public String getOrderStatusClass(String status) {
        if (status == null) {
            return "badge-secondary";
        }

        switch (status) {
            case "Chờ_xử_lý":
                return "badge-warning";
            case "Đã_xác_nhận":
                return "badge-info";
            case "Đang_giao":
                return "badge-primary";
            case "Đã_giao":
                return "badge-success";
            case "Đã_hủy":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    /**
     * Convert trạng thái tiếng Việt
     *
     * @param status Trạng thái (Còn_hàng, Hết_hàng, etc.)
     * @return Tên hiển thị
     */
    public String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.replace("_", " ");
    }
}