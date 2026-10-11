package com.example.expo.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Pavilion;
import com.example.expo.Service.PavilionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pavilion")
@RequiredArgsConstructor
public class PavilionController {

    private final PavilionService pavilionService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllPavilion() {
        return ResponseEntity.status(200).body(pavilionService.getAllPavilion());
    }

    @PostMapping("/add/{zoneId}/{locationId}")
    public ResponseEntity<?> addPavilion(@PathVariable Integer zoneId, @PathVariable Integer locationId, @RequestBody @Valid Pavilion pavilion) {
        pavilionService.addPavilion(zoneId, locationId, pavilion);
        return ResponseEntity.status(200).body(new ApiResponse("Pavilion added"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePavilion(@PathVariable Integer id, @RequestBody @Valid Pavilion pavilion) {
        pavilionService.updatePavilion(id, pavilion);
        return ResponseEntity.status(200).body(new ApiResponse("Pavilion updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePavilion(@PathVariable Integer id) {
        pavilionService.deletePavilion(id);
        return ResponseEntity.status(200).body(new ApiResponse("Pavilion deleted"));
    }
}
