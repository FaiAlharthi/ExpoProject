package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Profile;
import com.example.expo.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public List<Profile> getAllProfiles(){
        return profileRepository.findAll();
    }

    public void addProfile(Profile profile){
        if(profileRepository.findProfileByUsername(profile.getUsername()) != null){
            throw new ApiException("username already exists");
        }
        if(profileRepository.findProfileByEmail(profile.getEmail()) != null){
            throw new ApiException("email already exists");
        }
        //الرول يتحدد لما ينضاف Admin او Visitor
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
        profileRepository.delete(profile);
    }
}
