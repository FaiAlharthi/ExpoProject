package com.example.expo.Repository;

import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Integer> {
    Roadmap findRoadmapById(Integer id);

    // one roadmap per visitor per day
    Roadmap findRoadmapByVisitorAndVisitDate(Visitor visitor, LocalDate visitDate);

    // all roadmaps of the visitor, newest day first
    List<Roadmap> findRoadmapsByVisitorOrderByVisitDateDesc(Visitor visitor);
}
