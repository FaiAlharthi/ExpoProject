package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapService {
    private final RoadmapRepository roadmapRepository;
    private final VisitorRepository visitorRepository;
    private final PavilionRepository pavilionRepository;

    public List<Roadmap> getAllRoadmaps(){
        return roadmapRepository.findAll();
    }

    public void addRoadmap(Integer visitorId, Roadmap roadmap){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        checkDateAndTime(roadmap);
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setVisitor(visitor);
        roadmapRepository.save(roadmap);
    }

    public void updateRoadmap(Integer id, Roadmap roadmap){
        Roadmap oldRoadmap = roadmapRepository.findRoadmapById(id);
        if(oldRoadmap == null){
            throw new ApiException("roadmap not found");
        }
        checkDateAndTime(roadmap);
        oldRoadmap.setPlanType(roadmap.getPlanType());
        oldRoadmap.setVisitDate(roadmap.getVisitDate());
        oldRoadmap.setPartyType(roadmap.getPartyType());
        oldRoadmap.setPartySize(roadmap.getPartySize());
        oldRoadmap.setArrivalTime(roadmap.getArrivalTime());
        oldRoadmap.setDepartureTime(roadmap.getDepartureTime());
        oldRoadmap.setExperienceTypes(roadmap.getExperienceTypes());
        oldRoadmap.setVisitPace(roadmap.getVisitPace());
        oldRoadmap.setIncludePrayerBreaks(roadmap.getIncludePrayerBreaks());
        oldRoadmap.setIncludeMealBreaks(roadmap.getIncludeMealBreaks());
        oldRoadmap.setIncludeChildRestBreaks(roadmap.getIncludeChildRestBreaks());
        roadmapRepository.save(oldRoadmap);
    }

    public void deleteRoadmap(Integer id){
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        roadmapRepository.delete(roadmap);
    }

    public void addPavilionToRoadmap(Integer roadmapId, Integer pavilionId){
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("pavilion not found");
        }
        roadmap.getPavilions().add(pavilion);
        roadmapRepository.save(roadmap);
    }

    //التاريخ لازم يكون خلال فترة المعرض ، ووقت المغادرة بعد وقت الوصول
    private void checkDateAndTime(Roadmap roadmap){
        if(roadmap.getVisitDate().isBefore(LocalDate.of(2030, 10, 1)) || roadmap.getVisitDate().isAfter(LocalDate.of(2031, 3, 31))){
            throw new ApiException("visit date must be between 2030-10-01 and 2031-03-31");
        }
        if(!roadmap.getDepartureTime().isAfter(roadmap.getArrivalTime())){
            throw new ApiException("departure time must be after arrival time");
        }
    }
}
