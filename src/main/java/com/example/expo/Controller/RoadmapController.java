package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Dto.In.GenerateRoadmapRequest;
import com.example.expo.Dto.In.ReorderStopsRequest;
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

    @PostMapping("/createRoadmap/{visitorId}/{entranceId}")
    public ResponseEntity<?> createRoadmap(@PathVariable Integer visitorId, @PathVariable Integer entranceId, @RequestBody @Valid Roadmap roadmap){
        roadmapService.addRoadmap(visitorId, entranceId, roadmap);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap added"));
    }

    @DeleteMapping("/deleteRoadmap/{id}")
    public ResponseEntity<?> deleteRoadmap(@PathVariable Integer id){
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap deleted"));
    }

    @PostMapping("/generate/{visitorId}/{entranceId}")
    public ResponseEntity<?> generateRoadmap(@PathVariable Integer visitorId, @PathVariable Integer entranceId, @RequestBody @Valid GenerateRoadmapRequest request){
        return ResponseEntity.status(200).body(roadmapService.generateRoadmap(visitorId, entranceId, request));
    }

    @GetMapping("/getStops/{roadmapId}")
    public ResponseEntity<?> getStops(@PathVariable Integer roadmapId){
        return ResponseEntity.status(200).body(roadmapService.getStops(roadmapId));
    }

    @GetMapping("/visitor/{visitorId}")
    public ResponseEntity<?> getVisitorRoadmaps(@PathVariable Integer visitorId){
        return ResponseEntity.status(200).body(roadmapService.getVisitorRoadmaps(visitorId));
    }

    @PostMapping("/addStop/{roadmapId}/{pavilionId}/{position}")
    public ResponseEntity<?> addStop(@PathVariable Integer roadmapId, @PathVariable Integer pavilionId, @PathVariable Integer position){
        return ResponseEntity.status(200).body(roadmapService.addStop(roadmapId, pavilionId, position));
    }

    @DeleteMapping("/removeStop/{roadmapId}/{visitId}")
    public ResponseEntity<?> removeStop(@PathVariable Integer roadmapId, @PathVariable Integer visitId){
        return ResponseEntity.status(200).body(roadmapService.removeStop(roadmapId, visitId));
    }

    @PutMapping("/reorder/{roadmapId}")
    public ResponseEntity<?> reorderStops(@PathVariable Integer roadmapId, @RequestBody @Valid ReorderStopsRequest request){
        return ResponseEntity.status(200).body(roadmapService.reorderStops(roadmapId, request));
    }

    @PutMapping("/makeStatic/{roadmapId}")
    public ResponseEntity<?> makeStatic(@PathVariable Integer roadmapId){
        roadmapService.makeStatic(roadmapId);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap is static now"));
    }

    @PutMapping("/makeDynamic/{roadmapId}")
    public ResponseEntity<?> makeDynamic(@PathVariable Integer roadmapId){
        return ResponseEntity.status(200).body(roadmapService.makeDynamic(roadmapId));
    }

    /*
    @PutMapping("/updateRoadmap/{id}")
    public ResponseEntity<?> updateRoadmap(@PathVariable Integer id, @RequestBody @Valid Roadmap roadmap){
        roadmapService.updateRoadmap(id, roadmap);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap updated"));
    }
    */
}
