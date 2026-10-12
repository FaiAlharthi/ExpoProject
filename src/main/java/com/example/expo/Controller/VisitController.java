package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visit")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllVisits(){
        return ResponseEntity.status(200).body(visitService.getAllVisits());
    }

    @PostMapping("/createVisit/{passportId}/{pavilionId}")
    public ResponseEntity<?> createVisit(@PathVariable Integer passportId, @PathVariable Integer pavilionId){
        visitService.addVisit(passportId, pavilionId);
        return ResponseEntity.status(200).body(new ApiResponse("visit added"));
    }

    @PutMapping("/likeVisit/{id}")
    public ResponseEntity<?> likeVisit(@PathVariable Integer id){
        visitService.likeVisit(id);
        return ResponseEntity.status(200).body(new ApiResponse("visit like updated"));
    }

    @DeleteMapping("/deleteVisit/{id}")
    public ResponseEntity<?> deleteVisit(@PathVariable Integer id){
        visitService.deleteVisit(id);
        return ResponseEntity.status(200).body(new ApiResponse("visit deleted"));
    }
}
