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
        roadmap.setVisitor(visitor);
        roadmapRepository.save(roadmap);
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
}
