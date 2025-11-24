package com.example.do_an_tot_nghiep.controller;

import com.example.do_an_tot_nghiep.dto.ReviewDTO;
import com.example.do_an_tot_nghiep.model.Review;
import com.example.do_an_tot_nghiep.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewAdminController {

    private final IReviewService reviewService;

    /**
     * Danh sách reviews
     */
    @GetMapping
    public String listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            Model model) {

        log.info("Accessing review list - page: {}, search: {}, status: {}, rating: {}",
                page, search, status, rating);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDTO> reviewsPage;

        if (search != null && !search.trim().isEmpty()) {
            reviewsPage = reviewService.searchReviews(search, pageable);
        } else if (status != null && !status.isEmpty()) {
            Review.ReviewStatus reviewStatus = Review.ReviewStatus.valueOf(status);
            reviewsPage = reviewService.getReviewsByStatus(reviewStatus, pageable);
        } else if (rating != null) {
            reviewsPage = reviewService.getReviewsByRating(rating, pageable);
        } else {
            reviewsPage = reviewService.getAllReviews(pageable);
        }

        // Statistics
        Map<String, Long> stats = reviewService.getReviewStatistics();

        model.addAttribute("reviews", reviewsPage.getContent());
        model.addAttribute("currentPage", reviewsPage.getNumber());
        model.addAttribute("totalPages", reviewsPage.getTotalPages());
        model.addAttribute("totalItems", reviewsPage.getTotalElements());
        model.addAttribute("searchKeyword", search);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterRating", rating);
        model.addAttribute("stats", stats);

        return "review/review-list";
    }

    /**
     * Chi tiết review
     */
    @GetMapping("/{id}")
    public String viewReview(@PathVariable Integer id, Model model) {
        try {
            ReviewDTO review = reviewService.getReviewById(id);
            model.addAttribute("review", review);
            return "review/review-detail";
        } catch (Exception e) {
            log.error("Error loading review", e);
            return "redirect:/admin/reviews?error=notfound";
        }
    }

    /**
     * Approve review
     */
    @PostMapping("/approve/{id}")
    @ResponseBody
    public ResponseEntity<?> approveReview(@PathVariable Integer id) {
        try {
            reviewService.approveReview(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã duyệt đánh giá"));
        } catch (Exception e) {
            log.error("Error approving review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Reject review
     */
    @PostMapping("/reject/{id}")
    @ResponseBody
    public ResponseEntity<?> rejectReview(@PathVariable Integer id) {
        try {
            reviewService.rejectReview(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã từ chối đánh giá"));
        } catch (Exception e) {
            log.error("Error rejecting review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Reply review
     */
    @PostMapping("/reply/{id}")
    @ResponseBody
    public ResponseEntity<?> replyReview(
            @PathVariable Integer id,
            @RequestParam String reply,
            @RequestParam Integer employeeId) {
        try {
            reviewService.replyReview(id, reply, employeeId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã phản hồi đánh giá"));
        } catch (Exception e) {
            log.error("Error replying review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xóa review
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReview(@PathVariable Integer id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa đánh giá thành công"));
        } catch (Exception e) {
            log.error("Error deleting review", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xóa nhiều reviews
     */
    @PostMapping("/delete-batch")
    @ResponseBody
    public ResponseEntity<?> deleteBatch(@RequestBody List<Integer> ids) {
        try {
            int deletedCount = reviewService.deleteReviews(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deletedCount", deletedCount,
                    "message", "Đã xóa " + deletedCount + " đánh giá"
            ));
        } catch (Exception e) {
            log.error("Error deleting reviews", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Lấy thống kê
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Long> stats = reviewService.getReviewStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}