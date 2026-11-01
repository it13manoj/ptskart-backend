package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.Review;
import com.ptskart.api.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews(@RequestParam(required = false) Long productId) {
        List<Review> reviews = (productId != null)
                ? reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                : reviewRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.ok("Reviews retrieved successfully", reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> submitReview(@RequestBody Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        if (review.getStatus() == null) {
            review.setStatus("APPROVED");
        }
        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(ApiResponse.ok("Review submitted successfully", saved));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Review>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return reviewRepository.findById(id).map(r -> {
            r.setStatus(status);
            return ResponseEntity.ok(ApiResponse.ok("Review status updated", reviewRepository.save(r)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully"));
    }
}

