package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Dto.In.GenerateRoadmapRequest;
import com.example.expo.Dto.In.ReorderStopsRequest;
import com.example.expo.Service.JourneyPdfService;
import com.example.expo.Service.RoadmapService;
import jakarta.validation.Valid;
import com.example.expo.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapService roadmapService;
    private final JourneyPdfService journeyPdfService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllRoadmaps(){
        return ResponseEntity.status(200).body(roadmapService.getAllRoadmaps());
    }

    @GetMapping("/getMyRoadmaps")
    public ResponseEntity<?> getMyRoadmaps(@AuthenticationPrincipal User user){
        return ResponseEntity.status(200).body(roadmapService.getVisitorRoadmaps(user.getId()));
    }

    // the nearest entrance is chosen by the system
    @PostMapping("/generate")
    public ResponseEntity<?> generateRoadmap(@AuthenticationPrincipal User user, @RequestBody @Valid GenerateRoadmapRequest request){
        return ResponseEntity.status(200).body(roadmapService.generateRoadmap(user.getId(), request));
    }

    // only before the first visit
    @PutMapping("/changeEntrance/{roadmapId}/{entranceId}")
    public ResponseEntity<?> changeEntrance(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId, @PathVariable Integer entranceId){
        return ResponseEntity.status(200).body(roadmapService.changeEntrance(user.getId(), roadmapId, entranceId));
    }

    @GetMapping("/getStops/{roadmapId}")
    public ResponseEntity<?> getStops(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId){
        return ResponseEntity.status(200).body(roadmapService.getStops(user.getId(), roadmapId));
    }

    @PostMapping("/addStop/{roadmapId}/{pavilionId}/{position}")
    public ResponseEntity<?> addStop(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId, @PathVariable Integer pavilionId, @PathVariable Integer position){
        return ResponseEntity.status(200).body(roadmapService.addStop(user.getId(), roadmapId, pavilionId, position));
    }

    @DeleteMapping("/removeStop/{roadmapId}/{visitId}")
    public ResponseEntity<?> removeStop(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId, @PathVariable Integer visitId){
        return ResponseEntity.status(200).body(roadmapService.removeStop(user.getId(), roadmapId, visitId));
    }

    @PutMapping("/reorder/{roadmapId}")
    public ResponseEntity<?> reorderStops(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId, @RequestBody @Valid ReorderStopsRequest request){
        return ResponseEntity.status(200).body(roadmapService.reorderStops(user.getId(), roadmapId, request));
    }

    @PutMapping("/makeDynamic/{roadmapId}")
    public ResponseEntity<?> makeDynamic(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId){
        return ResponseEntity.status(200).body(roadmapService.makeDynamic(user.getId(), roadmapId));
    }

    @DeleteMapping("/deleteRoadmap/{id}")
    public ResponseEntity<?> deleteRoadmap(@AuthenticationPrincipal User user, @PathVariable Integer id){
        roadmapService.deleteRoadmap(user.getId(), id);
        return ResponseEntity.status(200).body(new ApiResponse("roadmap deleted"));
    }

    @GetMapping("/pdf/{roadmapId}")
    public ResponseEntity<?> getRoadmapPdf(@AuthenticationPrincipal User user, @PathVariable Integer roadmapId){
        byte[] pdf = journeyPdfService.getRoadmapPdf(user.getId(), roadmapId);
        return ResponseEntity.status(200)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("wijha-journey.pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    } // the roadmap as a PDF the visitor can share (map with the route, stops and stamps status)
}
