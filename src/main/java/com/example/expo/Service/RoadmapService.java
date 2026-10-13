package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Entrance;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.EntranceRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitRepository;
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
    private final VisitRepository visitRepository;
    private final EntranceRepository entranceRepository;

    public List<Roadmap> getAllRoadmaps(){
        return roadmapRepository.findAll();
    }

    public void addRoadmap(Integer visitorId, Integer entranceId, Roadmap roadmap){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Entrance entrance = entranceRepository.findEntranceById(entranceId);
        if(entrance == null){
            throw new ApiException("entrance not found");
        }
        checkDateAndTime(roadmap);
        if(roadmapRepository.findRoadmapByVisitorAndVisitDate(visitor, roadmap.getVisitDate()) != null){
            throw new ApiException("you already have a roadmap for this day, update it instead");
        }
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setVisitor(visitor);
        roadmap.setEntrance(entrance);
        roadmapRepository.save(roadmap);
    }

    public void updateRoadmap(Integer id, Roadmap roadmap){
        Roadmap oldRoadmap = roadmapRepository.findRoadmapById(id);
        if(oldRoadmap == null){
            throw new ApiException("roadmap not found");
        }
        checkDateAndTime(roadmap);
        Roadmap sameDayRoadmap = roadmapRepository.findRoadmapByVisitorAndVisitDate(oldRoadmap.getVisitor(), roadmap.getVisitDate());
        if(sameDayRoadmap != null && !sameDayRoadmap.getId().equals(id)){
            throw new ApiException("you already have a roadmap for this day");
        }
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
        oldRoadmap.setAllowRevisits(roadmap.getAllowRevisits());
        roadmapRepository.save(oldRoadmap);
    }

    public void deleteRoadmap(Integer id){
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        // keep visits, only unlink them
        if(roadmap.getVisits() != null){
            for(Visit visit : roadmap.getVisits()){
                visit.setRoadmap(null);
                visitRepository.save(visit);
            }
        }
        roadmapRepository.delete(roadmap);
    }

    // date must be in expo period, departure after arrival
    private void checkDateAndTime(Roadmap roadmap){
        if(roadmap.getVisitDate().isBefore(LocalDate.of(2030, 10, 1)) || roadmap.getVisitDate().isAfter(LocalDate.of(2031, 3, 31))){
            throw new ApiException("visit date must be between 2030-10-01 and 2031-03-31");
        }
        if(!roadmap.getDepartureTime().isAfter(roadmap.getArrivalTime())){
            throw new ApiException("departure time must be after arrival time");
        }
    }
}
