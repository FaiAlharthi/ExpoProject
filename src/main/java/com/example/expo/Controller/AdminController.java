package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.Model.Admin;
import com.example.expo.Service.AdminService;
import com.example.expo.Service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(){
        return ResponseEntity.status(200).body(dashboardService.getDashboard());
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllAdmins(){
        return ResponseEntity.status(200).body(adminService.getAllAdmins());
    }

    @PostMapping("/createAdmin/{profileId}")
    public ResponseEntity<?> createAdmin(@PathVariable Integer profileId, @RequestBody @Valid Admin admin){
        adminService.addAdmin(profileId, admin);
        return ResponseEntity.status(200).body(new ApiResponse("admin added"));
    }

    @DeleteMapping("/deleteAdmin/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Integer id){
        adminService.deleteAdmin(id);
        return ResponseEntity.status(200).body(new ApiResponse("admin deleted"));
    }

    @PutMapping("/updateAdmin/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Integer id, @RequestBody @Valid Admin admin){
        adminService.updateAdmin(id, admin);
        return ResponseEntity.status(200).body(new ApiResponse("admin updated"));
    }
}
