package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Admin;
import com.example.expo.Model.Profile;
import com.example.expo.Repository.AdminRepository;
import com.example.expo.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final ProfileRepository profileRepository;

    public List<Admin> getAllAdmins(){
        return adminRepository.findAll();
    }

    public void addAdmin(Integer profileId, Admin admin){
        Profile profile = profileRepository.findProfileById(profileId);
        if(profile == null){
            throw new ApiException("profile not found");
        }
        if(adminRepository.findAdminById(profileId) != null){
            throw new ApiException("this profile already has admin");
        }
        if(profile.getVisitor() != null){
            throw new ApiException("this profile is already a visitor");
        }
        profile.setRole("ADMIN");
        admin.setProfile(profile);
        adminRepository.save(admin);
    }

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
        admin.getProfile().setRole(null);
        adminRepository.delete(admin);
    }
}
