package com.example.expo.Controller;


import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Review;
import com.example.expo.Service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllReview() {
        return ResponseEntity.status(200).body(reviewService.getAllReviews());
    }

    @PostMapping("/add/{visitorId}/{pavilionId}")
    public ResponseEntity<?> addReview(@PathVariable Integer visitorId, @PathVariable Integer pavilionId, @RequestBody @Valid Review review) {
        reviewService.addReview(visitorId, pavilionId, review);
        return ResponseEntity.status(200).body(new ApiResponse("Review added"));
    }

    @PutMapping("/update/{visitorId}/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Integer visitorId, @PathVariable Integer reviewId, @RequestBody @Valid Review review) {
        reviewService.updateReview(visitorId, reviewId, review);
        return ResponseEntity.status(200).body(new ApiResponse("Review updated"));
    }

    @DeleteMapping("/delete/{visitorId}/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer visitorId, @PathVariable Integer reviewId) {
        reviewService.deleteReview(visitorId, reviewId);
        return ResponseEntity.status(200).body(new ApiResponse("Review deleted"));
    }

}
