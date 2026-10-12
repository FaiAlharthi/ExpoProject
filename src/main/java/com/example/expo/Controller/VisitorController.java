package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Visitor;
import com.example.expo.Service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visitor")
@RequiredArgsConstructor
public class VisitorController {
    private final VisitorService visitorService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllVisitors(){
        return ResponseEntity.status(200).body(visitorService.getAllVisitors());
    }

    @PostMapping("/createVisitor/{profileId}")
    public ResponseEntity<?> createVisitor(@PathVariable Integer profileId, @RequestBody @Valid Visitor visitor){
        visitorService.addVisitor(profileId, visitor);
        return ResponseEntity.status(200).body(new ApiResponse("visitor added"));
    }

    @DeleteMapping("/deleteVisitor/{id}")
    public ResponseEntity<?> deleteVisitor(@PathVariable Integer id){
        visitorService.deleteVisitor(id);
        return ResponseEntity.status(200).body(new ApiResponse("visitor deleted"));
    }

    @PutMapping("/{visitorId}/addInterest/{interestId}")
    public ResponseEntity<?> addInterestToVisitor(@PathVariable Integer visitorId, @PathVariable Integer interestId){
        visitorService.addInterestToVisitor(visitorId, interestId);
        return ResponseEntity.status(200).body(new ApiResponse("interest added to visitor"));
    }

    @PutMapping("/updateVisitor/{id}")
    public ResponseEntity<?> updateVisitor(@PathVariable Integer id, @RequestBody @Valid Visitor visitor){
        visitorService.updateVisitor(id, visitor);
        return ResponseEntity.status(200).body(new ApiResponse("visitor updated"));
    }
}
