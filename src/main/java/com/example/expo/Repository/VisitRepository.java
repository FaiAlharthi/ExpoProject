package com.example.expo.Repository;

import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {
    Visit findVisitById(Integer id);

    // checks if this passport visited the pavilion on the same day
    Boolean existsByPassportAndPavilionAndVisitedAtBetween(Passport passport, Pavilion pavilion, LocalDateTime start, LocalDateTime end);

    // counts real visits only
    Long countByPassportAndIsVisitedTrue(Passport passport);

    List<Visit> findVisitsByRoadmapAndIsVisitedFalse(Roadmap roadmap);

    List<Visit> findVisitsByRoadmapOrderByStopOrderAsc(Roadmap roadmap);

    List<Visit> findVisitsByPassportAndIsVisitedTrue(Passport passport);

    // planned stop for this pavilion in the roadmap
    Visit findVisitByRoadmapAndPavilionAndIsVisitedFalse(Roadmap roadmap, Pavilion pavilion);
}
