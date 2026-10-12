package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Visit;
import com.example.expo.Repository.PassportRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.VisitRepository;
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
        //نفس الجناح ما ينحسب مرتين في نفس اليوم
        LocalDate today = LocalDate.now();
        if(visitRepository.existsByPassportAndPavilionAndVisitedAtBetween(passport, pavilion, today.atStartOfDay(), today.atTime(LocalTime.MAX))){
            throw new ApiException("this pavilion is already visited today");
        }
        Visit visit = new Visit();
        visit.setVisitedAt(LocalDateTime.now());
        visit.setIsLiked(false);
        visit.setPassport(passport);
        visit.setPavilion(pavilion);
        visitRepository.save(visit);
        updateExplorerLevel(passport);
    }

    public void likeVisit(Integer id){
        Visit visit = visitRepository.findVisitById(id);
        if(visit == null){
            throw new ApiException("visit not found");
        }
        visit.setIsLiked(!visit.getIsLiked());
        visitRepository.save(visit);
    }

    public void deleteVisit(Integer id){
        Visit visit = visitRepository.findVisitById(id);
        if(visit == null){
            throw new ApiException("visit not found");
        }
        Passport passport = visit.getPassport();
        passport.getVisits().remove(visit);
        visitRepository.delete(visit);
        updateExplorerLevel(passport);
    }

    //BRONZE من 0 ، SILVER من 10 ، GOLD من 25
    private void updateExplorerLevel(Passport passport){
        Long visitsCount = visitRepository.countByPassport(passport);
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
