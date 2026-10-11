package com.example.expo.Repository;

import com.example.expo.Model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Integer> {
    Roadmap findRoadmapById(Integer id);
}
