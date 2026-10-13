package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PassportRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final PassportRepository passportRepository;
    private final PavilionRepository pavilionRepository;
    private final VisitorRepository visitorRepository;
    private final RoadmapRepository roadmapRepository;

    public List<Visit> getAllVisits(){
        return visitRepository.findAll();
    }

    public void addVisit(Integer passportId, Integer pavilionId){
        Passport passport = passportRepository.findPassportById(passportId);
        if(passport == null){
            throw new ApiException("passport not found");
        }
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("pavilion not found");
        }
        // one visit per pavilion per day
        LocalDate today = LocalDate.now();
        if(visitRepository.existsByPassportAndPavilionAndVisitedAtBetween(passport, pavilion, today.atStartOfDay(), today.atTime(LocalTime.MAX))){
            throw new ApiException("this pavilion is already visited today");
        }
        Visit visit = new Visit();
        visit.setVisitedAt(LocalDateTime.now());
        visit.setIsVisited(true);
        visit.setPassport(passport);
        visit.setPavilion(pavilion);
        visitRepository.save(visit);
        updateExplorerLevel(passport);
    }

    // works on any pavilion, if it is planned in today's roadmap it gets marked as visited
    public void checkIn(Integer visitorId, Integer pavilionId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Passport passport = visitor.getPassport();
        if(passport == null){
            throw new ApiException("visitor has no passport");
        }
        Pavilion pavilion = pavilionRepository.findPavilionById(pavilionId);
        if(pavilion == null){
            throw new ApiException("pavilion not found");
        }
        LocalDate today = LocalDate.now();
        if(visitRepository.existsByPassportAndPavilionAndVisitedAtBetween(passport, pavilion, today.atStartOfDay(), today.atTime(LocalTime.MAX))){
            throw new ApiException("this pavilion is already visited today");
        }
        Visit visit = null;
        Roadmap todayRoadmap = roadmapRepository.findRoadmapByVisitorAndVisitDate(visitor, today);
        if(todayRoadmap != null){
            visit = visitRepository.findVisitByRoadmapAndPavilionAndIsVisitedFalse(todayRoadmap, pavilion);
        }
        if(visit == null){
            visit = new Visit();
            visit.setPavilion(pavilion);
        }
        visit.setPassport(passport);
        visit.setIsVisited(true);
        visit.setVisitedAt(LocalDateTime.now());
        visitRepository.save(visit);
        updateExplorerLevel(passport);
    }

    public void deleteVisit(Integer id){
        Visit visit = visitRepository.findVisitById(id);
        if(visit == null){
            throw new ApiException("visit not found");
        }
        // planned stops have no passport yet
        Passport passport = visit.getPassport();
        if(passport != null){
            passport.getVisits().remove(visit);
        }
        visitRepository.delete(visit);
        if(passport != null){
            updateExplorerLevel(passport);
        }
    }

    // BRONZE 0+, SILVER 10+, GOLD 25+
    private void updateExplorerLevel(Passport passport){
        Long visitsCount = visitRepository.countByPassportAndIsVisitedTrue(passport);
        if(visitsCount >= 25){
            passport.setExplorerLevel("GOLD");
        }else if(visitsCount >= 10){
            passport.setExplorerLevel("SILVER");
        }else{
            passport.setExplorerLevel("BRONZE");
        }
        passportRepository.save(passport);
    }
}
