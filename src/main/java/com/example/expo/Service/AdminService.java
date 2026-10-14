package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Admin;
import com.example.expo.Model.Profile;
import com.example.expo.Repository.AdminRepository;
import com.example.expo.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final ProfileRepository profileRepository;

    public void updateAdmin(Integer id, Admin admin){
        Admin oldAdmin = adminRepository.findAdminById(id);
        if(oldAdmin == null){
            throw new ApiException("admin not found");
        }
        oldAdmin.setJobTitle(admin.getJobTitle());
        adminRepository.save(oldAdmin);
    }

    public void deleteAdmin(Integer id){
        Admin admin = adminRepository.findAdminById(id);
        if(admin == null){
            throw new ApiException("admin not found");
        }
        admin.getProfile().setAdmin(null);
        // back to the default role
        admin.getProfile().setRole("VISITOR");
        adminRepository.delete(admin);
    }
}
