package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.ReviewDTO;
import com.example.do_an_tot_nghiep.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IReviewService {

    /**
     * Lấy tất cả reviews có phân trang
     */
    Page<ReviewDTO> getAllReviews(Pageable pageable);

    /**
     * Lọc theo trạng thái
     */
    Page<ReviewDTO> getReviewsByStatus(Review.ReviewStatus status, Pageable pageable);

    /**
     * Lọc theo rating
     */
    Page<ReviewDTO> getReviewsByRating(Integer rating, Pageable pageable);

    /**
     * Lọc theo sản phẩm
     */
    Page<ReviewDTO> getReviewsByProduct(Integer deviceId, Pageable pageable);

    /**
     * Tìm kiếm reviews
     */
    Page<ReviewDTO> searchReviews(String keyword, Pageable pageable);

    /**
     * Lấy review theo ID
     */
    ReviewDTO getReviewById(Integer id);

    /**
     * Approve review
     */
    void approveReview(Integer id);

    /**
     * Reject review
     */
    void rejectReview(Integer id);

    /**
     * Admin reply
     */
    void replyReview(Integer id, String reply, Integer employeeId);

    /**
     * Xóa review
     */
    void deleteReview(Integer id);

    /**
     * Xóa nhiều reviews
     */
    int deleteReviews(List<Integer> ids);

    /**
     * Đếm reviews theo trạng thái
     */
    Map<String, Long> getReviewStatistics();

    /**
     * Lấy rating trung bình của sản phẩm
     */
    Double getAverageRatingByProduct(Integer deviceId);

    /**
     * Kiểm tra customer đã review chưa
     */
    boolean hasCustomerReviewedProduct(Integer customerId, Integer deviceId);

    /**
     * Lấy reviews chưa reply
     */
    Page<ReviewDTO> getReviewsWithoutReply(Pageable pageable);
}