package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Dto.Out.PassportResponse;
import com.example.expo.Dto.Out.PassportVisitResponse;
import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PassportRepository;
import com.example.expo.Repository.VisitRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PassportService {
    private final PassportRepository passportRepository;
    private final VisitorRepository visitorRepository;
    private final VisitRepository visitRepository;

    public List<Passport> getAllPassports(){
        return passportRepository.findAll();
    }

    // the visitor passport with real visits only
    public PassportResponse getVisitorPassport(Integer visitorId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Passport passport = visitor.getPassport();
        if(passport == null){
            throw new ApiException("passport not found");
        }
        List<Visit> visits = visitRepository.findVisitsByPassportAndIsVisitedTrue(passport);
        visits.sort(Comparator.comparing(Visit::getVisitedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        List<PassportVisitResponse> stamps = new ArrayList<>();
        for(Visit visit : visits){
            Pavilion pavilion = visit.getPavilion();
            stamps.add(new PassportVisitResponse(visit.getId(), pavilion.getId(), pavilion.getName(), pavilion.getCountry(),
                    pavilion.getZone() == null ? null : pavilion.getZone().getName(), pavilion.getImageUrl(), visit.getVisitedAt()));
        }
        return new PassportResponse(passport.getId(), passport.getPassportNumber(), stamps.size(), stamps);
    }

}
