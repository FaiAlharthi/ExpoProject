package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Passport;
import com.example.expo.Service.PassportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/passport")
@RequiredArgsConstructor
public class PassportController {
    private final PassportService passportService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllPassports(){
        return ResponseEntity.status(200).body(passportService.getAllPassports());
    }

    @PostMapping("/createPassport/{visitorId}")
    public ResponseEntity<?> createPassport(@PathVariable Integer visitorId, @RequestBody @Valid Passport passport){
        passportService.addPassport(visitorId, passport);
        return ResponseEntity.status(200).body(new ApiResponse("passport added"));
    }

    @DeleteMapping("/deletePassport/{id}")
    public ResponseEntity<?> deletePassport(@PathVariable Integer id){
        passportService.deletePassport(id);
        return ResponseEntity.status(200).body(new ApiResponse("passport deleted"));
    }
}
