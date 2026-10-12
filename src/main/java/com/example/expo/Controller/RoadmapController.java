package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Roadmap;
import com.example.expo.Service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllRoadmaps(){
        return ResponseEntity.status(200).body(roadmapService.getAllRoadmaps());
    }

    @PostMapping("/createRoadmap/{visitorId}")
    public ResponseEntity<?> createRoadmap(@PathVariable Integer visitorId, @RequestBody @Valid Roadmap roadmap){
        roadmapService.addRoadmap(visitorId, roadmap);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap added"));
    }

    @DeleteMapping("/deleteRoadmap/{id}")
    public ResponseEntity<?> deleteRoadmap(@PathVariable Integer id){
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap deleted"));
    }

    @PutMapping("/{roadmapId}/addPavilion/{pavilionId}")
    public ResponseEntity<?> addPavilionToRoadmap(@PathVariable Integer roadmapId, @PathVariable Integer pavilionId){
        roadmapService.addPavilionToRoadmap(roadmapId, pavilionId);
        return ResponseEntity.status(200).body(new ApiResponse("pavilion added to roadmap"));
    }

    @PutMapping("/updateRoadmap/{id}")
    public ResponseEntity<?> updateRoadmap(@PathVariable Integer id, @RequestBody @Valid Roadmap roadmap){
        roadmapService.updateRoadmap(id, roadmap);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap updated"));
    }
}
