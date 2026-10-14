package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Profile;
import com.example.expo.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Integer id){
        return ResponseEntity.status(200).body(profileService.getProfile(id));
    }

    @DeleteMapping("/deleteProfile/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable Integer id){
        profileService.deleteProfile(id);
        return ResponseEntity.status(200).body(new ApiResponse("profile deleted"));
    }

    @PutMapping("/updateProfile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Integer id, @RequestBody @Valid Profile profile){
        profileService.updateProfile(id, profile);
        return ResponseEntity.status(200).body(new ApiResponse("profile updated"));
    }
}
