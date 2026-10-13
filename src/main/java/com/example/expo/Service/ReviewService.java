package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.*;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ReviewRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if(passport==null||passport.getVisits()==null){
            throw new ApiException("You must visit the pavilion before reviewing it");
        }
        boolean visited = false;
        for(Visit visit : passport.getVisits()){
            if(visit.getIsVisited() && visit.getPavilion().getId().equals(pavilionId)){
                visited = true;
            }
        }
        if(!visited){
            throw new ApiException("You must visit the pavilion before reviewing it");
        }


        if(reviewRepository.findReviewByVisitorAndPavilion(visitor, pavilion) != null){
            throw new ApiException("You already reviewed this pavilion");
        }
        review.setVisitor(visitor);
        review.setPavilion(pavilion);
        review.setCreatedAt(LocalDateTime.now());
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

    // متوسط تقييم جناح
    public Double getPavilionAverageRating(Integer pavilionId){
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("Pavilion not found");
        }
        Double average = reviewRepository.getAverageRating(pavilionId);
        if(average == null){
            return 0.0;
        }
        return average;
    }


    // الأجنحة اللي زارها وما قيّمها
    public List<Pavilion> getPendingReviews(Integer visitorId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("Visitor not found");
        }
        if(visitor.getPassport() == null || visitor.getPassport().getVisits() == null){
            throw new ApiException("You did not visit any pavilion yet");
        }
        List<Pavilion> pending = new ArrayList<>();
        for(Visit visit : visitor.getPassport().getVisits()){
            if(reviewRepository.findReviewByVisitorAndPavilion(visitor, visit.getPavilion()) == null){
                pending.add(visit.getPavilion());
            }
        }
        return pending;
    }


    // تقييماتي
    public List<Review> getVisitorReviews(Integer visitorId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("Visitor not found");
        }
        return reviewRepository.findReviewsByVisitor(visitor);
    }

    // أحدث تقييمات جناح
    public List<Review> getLatestReviews(Integer pavilionId){
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("Pavilion not found");
        }
        return reviewRepository.findReviewsByPavilionOrderByCreatedAtDesc(pavilion);
    }




}
