package com.example.expo.Service;

import com.example.expo.Dto.Out.AdminDashboardResponse;
import com.example.expo.Dto.Out.CountResponse;
import com.example.expo.Dto.Out.PavilionStatResponse;
import com.example.expo.Model.Interest;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Review;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ReviewRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// statistics for the admin, calculated from the saved data
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VisitorRepository visitorRepository;
    private final VisitRepository visitRepository;
    private final RoadmapRepository roadmapRepository;
    private final ReviewRepository reviewRepository;
    private final PavilionRepository pavilionRepository;

    // how many items in each top list
    private static final int TOP_LIMIT = 5;

    public AdminDashboardResponse getDashboard(){
        List<Visit> visits = visitRepository.findVisitsByIsVisitedTrue();
        List<Roadmap> roadmaps = roadmapRepository.findAll();

        // visits and reviews of each pavilion
        Map<Integer, Long> visitsByPavilion = new HashMap<>();
        Map<String, Long> visitsPerDay = new TreeMap<>();
        Map<String, Long> visitsPerZone = new HashMap<>();
        for(Visit visit : visits){
            Pavilion pavilion = visit.getPavilion();
            visitsByPavilion.merge(pavilion.getId(), 1L, Long::sum);
            if(visit.getVisitedAt() != null){
                visitsPerDay.merge(visit.getVisitedAt().toLocalDate().toString(), 1L, Long::sum);
            }
            if(pavilion.getZone() != null){
                visitsPerZone.merge(pavilion.getZone().getName(), 1L, Long::sum);
            }
        }
        Map<Integer, Long> ratingSum = new HashMap<>();
        Map<Integer, Long> ratingCount = new HashMap<>();
        for(Review review : reviewRepository.findAll()){
            Integer pavilionId = review.getPavilion().getId();
            ratingSum.merge(pavilionId, (long) review.getRating(), Long::sum);
            ratingCount.merge(pavilionId, 1L, Long::sum);
        }

        List<PavilionStatResponse> pavilions = new ArrayList<>();
        for(Pavilion pavilion : pavilionRepository.findAll()){
            Long count = ratingCount.getOrDefault(pavilion.getId(), 0L);
            Double average = count == 0 ? null : Math.round(ratingSum.get(pavilion.getId()) * 10.0 / count) / 10.0;
            pavilions.add(new PavilionStatResponse(pavilion.getId(), pavilion.getName(), pavilion.getCountry(),
                    pavilion.getZone() == null ? null : pavilion.getZone().getName(),
                    visitsByPavilion.getOrDefault(pavilion.getId(), 0L), average, count));
        }

        // most visited, only pavilions with visits
        List<PavilionStatResponse> mostVisited = new ArrayList<>(pavilions.stream().filter(p -> p.getVisits() > 0).toList());
        mostVisited.sort(Comparator.comparing(PavilionStatResponse::getVisits).reversed());

        // rated pavilions only, more reviews first when the average is the same
        List<PavilionStatResponse> rated = pavilions.stream().filter(p -> p.getAverageRating() != null).toList();
        List<PavilionStatResponse> topRated = new ArrayList<>(rated);
        topRated.sort(Comparator.comparing(PavilionStatResponse::getAverageRating).reversed()
                .thenComparing(Comparator.comparing(PavilionStatResponse::getReviewsCount).reversed()));
        List<PavilionStatResponse> lowestRated = new ArrayList<>(rated);
        lowestRated.sort(Comparator.comparing(PavilionStatResponse::getAverageRating)
                .thenComparing(Comparator.comparing(PavilionStatResponse::getReviewsCount).reversed()));

        Map<String, Long> interests = new HashMap<>();
        List<Visitor> visitors = visitorRepository.findAll();
        for(Visitor visitor : visitors){
            if(visitor.getInterests() != null){
                for(Interest interest : visitor.getInterests()){
                    interests.merge(interest.getName(), 1L, Long::sum);
                }
            }
        }

        Map<String, Long> entrances = new HashMap<>();
        for(Roadmap roadmap : roadmaps){
            if(roadmap.getEntrance() != null){
                entrances.merge(roadmap.getEntrance().getName(), 1L, Long::sum);
            }
        }

        return new AdminDashboardResponse(
                (long) visitors.size(),
                (long) visits.size(),
                (long) roadmaps.size(),
                top(mostVisited),
                top(topRated),
                top(lowestRated),
                top(toCounts(interests, true)),
                toCounts(visitsPerDay, false),
                toCounts(visitsPerZone, true),
                toCounts(entrances, true));
    }

    private <T> List<T> top(List<T> items){
        return items.size() > TOP_LIMIT ? items.subList(0, TOP_LIMIT) : items;
    }

    // sorted by count when asked, otherwise keeps the map order (days)
    private List<CountResponse> toCounts(Map<String, Long> map, boolean sortByCount){
        List<CountResponse> counts = new ArrayList<>();
        for(Map.Entry<String, Long> entry : map.entrySet()){
            counts.add(new CountResponse(entry.getKey(), entry.getValue()));
        }
        if(sortByCount){
            counts.sort(Comparator.comparing(CountResponse::getCount).reversed());
        }
        return counts;
    }
}
