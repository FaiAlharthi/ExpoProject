package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Location;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Zone;
import com.example.expo.Repository.LocationRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PavilionService {

    private final PavilionRepository pavilionRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;

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
        oldPavilion.setDescription(pavilion.getDescription());
        oldPavilion.setVisitDuration(pavilion.getVisitDuration());
        oldPavilion.setImageUrl(pavilion.getImageUrl());
        pavilionRepository.save(oldPavilion);
    }

    public void deletePavilion(Integer id){
        Pavilion pavilion= pavilionRepository.findPavilionById(id);
        if(pavilion ==null){
            throw new ApiException("Pavilion not found");
        }
        if(pavilion.getRoadmaps()!=null&&!pavilion.getRoadmaps().isEmpty()){
            throw new ApiException("Pavilion is used in roadmaps");
        }
        if(pavilion.getVisits()!=null&&!pavilion.getVisits().isEmpty()){
            throw new ApiException("Pavilion has visits");
        }
        pavilionRepository.delete(pavilion);
    }


}
