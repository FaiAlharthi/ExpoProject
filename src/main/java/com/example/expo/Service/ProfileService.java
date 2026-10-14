package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Dto.Out.ProfileResponse;
import com.example.expo.Model.Profile;
import com.example.expo.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final VisitorService visitorService;

    public List<ProfileResponse> getAllProfiles(){
        List<ProfileResponse> profiles = new ArrayList<>();
        for(Profile profile : profileRepository.findAll()){
            profiles.add(toProfileResponse(profile));
        }
        return profiles;
    }

    public ProfileResponse getProfile(Integer id){
        Profile profile = profileRepository.findProfileById(id);
        if(profile == null){
            throw new ApiException("profile not found");
        }
        return toProfileResponse(profile);
    }

    private ProfileResponse toProfileResponse(Profile profile){
        return new ProfileResponse(profile.getId(), profile.getFullName(), profile.getUsername(), profile.getEmail(),
                profile.getPhone(), profile.getRole(), profile.getCreatedAt());
    }

    public void addProfile(Profile profile){
        if(profileRepository.findProfileByUsername(profile.getUsername()) != null){
            throw new ApiException("username already exists");
        }
        if(profileRepository.findProfileByEmail(profile.getEmail()) != null){
            throw new ApiException("email already exists");
        }
        // role is set when an admin or visitor is added
        profile.setRole(null);
        profile.setCreatedAt(LocalDateTime.now());
        profileRepository.save(profile);
    }

    public void updateProfile(Integer id, Profile profile){
        Profile oldProfile = profileRepository.findProfileById(id);
        if(oldProfile == null){
            throw new ApiException("profile not found");
        }
        Profile usernameOwner = profileRepository.findProfileByUsername(profile.getUsername());
        if(usernameOwner != null && !usernameOwner.getId().equals(id)){
            throw new ApiException("username already exists");
        }
        Profile emailOwner = profileRepository.findProfileByEmail(profile.getEmail());
        if(emailOwner != null && !emailOwner.getId().equals(id)){
            throw new ApiException("email already exists");
        }
        oldProfile.setFullName(profile.getFullName());
        oldProfile.setUsername(profile.getUsername());
        oldProfile.setPassword(profile.getPassword());
        oldProfile.setEmail(profile.getEmail());
        oldProfile.setPhone(profile.getPhone());
        profileRepository.save(oldProfile);
    }

    public void deleteProfile(Integer id){
        Profile profile = profileRepository.findProfileById(id);
        if(profile == null){
            throw new ApiException("profile not found");
        }
        if(profile.getVisitor() != null){
            visitorService.clearRoadmapStops(profile.getVisitor());
        }
        profileRepository.delete(profile);
    }
}
