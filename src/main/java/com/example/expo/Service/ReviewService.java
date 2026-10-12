package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Review;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ReviewRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final VisitorRepository visitorRepository;
    private final PavilionRepository pavilionRepository;

    public List<Review>getAllReviews(){
        return reviewRepository.findAll();
    }

    public void addReview(Integer visitorId,Integer pavilionId, Review review){
        Visitor visitor=visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("Visitor not found");
        }
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("Pavilion not found");
        }
        Passport passport=visitor.getPassport();
        if(passport==null||passport.getPavilions()==null||!passport.getPavilions().contains(pavilion)){
            throw new ApiException("You must visit the pavilion before reviewing it");
        }

        if(reviewRepository.findReviewByVisitorAndPavilion(visitor, pavilion) != null){
            throw new ApiException("You already reviewed this pavilion");
        }
        review.setVisitor(visitor);
        review.setPavilion(pavilion);
        reviewRepository.save(review);
    }
    public void updateReview(Integer visitorId, Integer reviewId, Review review){
        Review oldReview = reviewRepository.findReviewById(reviewId);
        if(oldReview == null){
            throw new ApiException("Review not found");
        }
        if(!oldReview.getVisitor().getId().equals(visitorId)){
            throw new ApiException("You can only update your own review");
        }
        oldReview.setRating(review.getRating());
        oldReview.setComment(review.getComment());
        reviewRepository.save(oldReview);
    }

    public void deleteReview(Integer visitorId, Integer reviewId){
        Review review = reviewRepository.findReviewById(reviewId);
        if(review == null){
            throw new ApiException("Review not found");
        }
        if(!review.getVisitor().getId().equals(visitorId)){
            throw new ApiException("You can only delete your own review");
        }
        reviewRepository.delete(review);
    }




}
