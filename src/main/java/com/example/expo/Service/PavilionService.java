package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.*;
import com.example.expo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PavilionService {

    private final PavilionRepository pavilionRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;
    private final VisitorRepository visitorRepository;
    private final ReviewRepository reviewRepository;

    public List<Pavilion>getAllPavilion(){
        return pavilionRepository.findAll();
    }

    public void addPavilion(Integer zoneId, Integer locationId,Pavilion pavilion){
        Zone zone=zoneRepository.findZoneById(zoneId);
        if(zone==null){
            throw new ApiException("Zone not found");
        }
        Location location=locationRepository.findLocationById(locationId);
        if(location==null){
            throw new ApiException("Location not found");
        }
        if(location.getZone()!=null||location.getPavilion()!= null){
            throw new ApiException("Location is already used");
        }
        if(pavilionRepository.findPavilionByName(pavilion.getName())!=null){
            throw new ApiException("Pavilion already exists");
        }
        pavilion.setZone(zone);
        pavilion.setLocation(location);
        pavilionRepository.save(pavilion);
    }

    public void updatePavilion(Integer id, Pavilion pavilion){
        Pavilion oldPavilion = pavilionRepository.findPavilionById(id);
        if(oldPavilion==null){
            throw new ApiException("Pavilion not found");
        }
        Pavilion samePavilion = pavilionRepository.findPavilionByName(pavilion.getName());
        if(samePavilion!=null&&!samePavilion.getId().equals(id)){
            throw new ApiException("Pavilion already exists");
        }
        oldPavilion.setName(pavilion.getName());
        oldPavilion.setCountry(pavilion.getCountry());
        oldPavilion.setDescription(pavilion.getDescription());

        oldPavilion.setImageUrl(pavilion.getImageUrl());
        pavilionRepository.save(oldPavilion);
    }

    public void deletePavilion(Integer id){
        Pavilion pavilion= pavilionRepository.findPavilionById(id);
        if(pavilion ==null){
            throw new ApiException("Pavilion not found");
        }
        if(pavilion.getVisits()!=null&&!pavilion.getVisits().isEmpty()){
            throw new ApiException("Pavilion has visits");
        }
        pavilionRepository.delete(pavilion);
    }

     //endpoint أجنحة زون معينة
    public List<Pavilion>getPavilionsByZone(Integer zone_id){
        Zone zone=zoneRepository.findZoneById(zone_id);
        if(zone == null){
            throw new ApiException("Zone not found");
        }
        return pavilionRepository.findPavilionByZone(zone);
    }

    //endpoint ابحث بالاسم عن جناح
    public List<Pavilion>searchPavilion(String name){
        List<Pavilion>pavilions=pavilionRepository.findPavilionsByNameContaining(name);
        if(pavilions.isEmpty()){
            throw new ApiException("No pavilions found");
        }
        return pavilions;
    }

    //3) الأجنحة مرتبة حسب التقييم
    public List<Pavilion> getTopRatedPavilions(){
        List<Pavilion> pavilions = pavilionRepository.findTopRatedPavilions();
        if(pavilions.isEmpty()){
            throw new ApiException("No rated pavilions yet");
        }
        return pavilions;
    }

    // 4) الأجنحة اللي ما زارها الزائر
    public List<Pavilion> getUnvisitedPavilions(Integer visitorId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("Visitor not found");
        }
        List<Pavilion> unvisited = new ArrayList<>();
        for(Pavilion pavilion : pavilionRepository.findAll()){
            boolean visited=false;
            if(visitor.getPassport() != null && visitor.getPassport().getVisits() != null){
                for(Visit visit : visitor.getPassport().getVisits()){
                    if(visit.getIsVisited() && visit.getPavilion().getId().equals(pavilion.getId())){
                        visited = true;
                    }
                }
            }
            if(!visited){
                unvisited.add(pavilion);
            }
        }
        return unvisited;
    }






}
