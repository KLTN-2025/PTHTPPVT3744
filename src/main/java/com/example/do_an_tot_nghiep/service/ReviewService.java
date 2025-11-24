package com.example.do_an_tot_nghiep.service;

import com.example.do_an_tot_nghiep.dto.ReviewDTO;
import com.example.do_an_tot_nghiep.model.Employee;
import com.example.do_an_tot_nghiep.model.Review;
import com.example.do_an_tot_nghiep.repository.IEmployeeRepository;
import com.example.do_an_tot_nghiep.repository.IReviewRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IEmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<ReviewDTO> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getReviewsByStatus(Review.ReviewStatus status, Pageable pageable) {
        return reviewRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getReviewsByRating(Integer rating, Pageable pageable) {
        return reviewRepository.findByRating(rating, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> getReviewsByProduct(Integer deviceId, Pageable pageable) {
        return reviewRepository.findByDevice_DeviceId(deviceId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<ReviewDTO> searchReviews(String keyword, Pageable pageable) {
        return reviewRepository.searchReviews(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public ReviewDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá với ID: " + id));
        return convertToDTO(review);
    }

    @Override
    @Transactional
    public void approveReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        review.setStatus(Review.ReviewStatus.APPROVED);
        reviewRepository.save(review);
        log.info("Approved review id: {}", id);
    }

    @Override
    @Transactional
    public void rejectReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        review.setStatus(Review.ReviewStatus.REJECTED);
        reviewRepository.save(review);
        log.info("Rejected review id: {}", id);
    }

    @Override
    @Transactional
    public void replyReview(Integer id, String reply, Integer employeeId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        review.setAdminReply(reply);
        review.setRepliedBy(employee);
        review.setRepliedAt(LocalDateTime.now());

        reviewRepository.save(review);
        log.info("Admin replied to review id: {}", id);
    }

    @Override
    @Transactional
    public void deleteReview(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        reviewRepository.delete(review);
        log.info("Deleted review id: {}", id);
    }

    @Override
    @Transactional
    public int deleteReviews(List<Integer> ids) {
        int deletedCount = 0;
        for (Integer id : ids) {
            try {
                deleteReview(id);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete review id: {}", id, e);
            }
        }
        return deletedCount;
    }

    @Override
    public Map<String, Long> getReviewStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", reviewRepository.count());
        stats.put("pending", reviewRepository.countByStatus(Review.ReviewStatus.PENDING));
        stats.put("approved", reviewRepository.countByStatus(Review.ReviewStatus.APPROVED));
        stats.put("rejected", reviewRepository.countByStatus(Review.ReviewStatus.REJECTED));
        stats.put("rating5", reviewRepository.countByRating(5));
        stats.put("rating4", reviewRepository.countByRating(4));
        stats.put("rating3", reviewRepository.countByRating(3));
        stats.put("rating2", reviewRepository.countByRating(2));
        stats.put("rating1", reviewRepository.countByRating(1));
        return stats;
    }

    @Override
    public Double getAverageRatingByProduct(Integer deviceId) {
        Double avg = reviewRepository.getAverageRatingByDevice(deviceId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public boolean hasCustomerReviewedProduct(Integer customerId, Integer deviceId) {
        return reviewRepository.hasCustomerReviewedProduct(customerId, deviceId);
    }

    @Override
    public Page<ReviewDTO> getReviewsWithoutReply(Pageable pageable) {
        return reviewRepository.findReviewsWithoutReply(pageable)
                .map(this::convertToDTO);
    }

    // Helper methods
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());

        // Customer info
        if (review.getCustomer() != null) {
            dto.setCustomerId(review.getCustomer().getCustomerId());
            dto.setCustomerName(review.getCustomer().getFullName());
            dto.setCustomerAvatar(review.getCustomer().getAvatarUrl());
            dto.setCustomerEmail(review.getCustomer().getEmail());
        }

        // Product info
        if (review.getDevice() != null) {
            dto.setDeviceId(review.getDevice().getDeviceId());
            dto.setDeviceName(review.getDevice().getName());
            dto.setDeviceImage(review.getDevice().getImageUrl());
        }

        // Order info
        if (review.getOrder() != null) {
            dto.setOrderId(review.getOrder().getOrderId());
            dto.setOrderCode(review.getOrder().getOrderCode());
        }

        // Review content
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImages(review.getImages());
        dto.setImageList(parseImages(review.getImages()));
        dto.setIsVerifiedPurchase(review.getIsVerifiedPurchase());

        // Admin reply
        dto.setAdminReply(review.getAdminReply());
        if (review.getRepliedBy() != null) {
            dto.setRepliedById(review.getRepliedBy().getEmployeeId());
            dto.setRepliedByName(review.getRepliedBy().getFullName());
        }
        dto.setRepliedAt(review.getRepliedAt());

        // Status
        dto.setStatus(review.getStatus());
        dto.setStatusDisplay(getStatusDisplay(review.getStatus()));

        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Try parse as JSON array
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If not JSON, try comma-separated
            return Arrays.asList(imagesJson.split(","));
        }
    }

    private String getStatusDisplay(Review.ReviewStatus status) {
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case APPROVED: return "Đã duyệt";
            case REJECTED: return "Đã từ chối";
            default: return status.name();
        }
    }
}