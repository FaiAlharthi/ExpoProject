package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Profile;
import com.example.expo.Repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public List<Profile> getAllProfiles(){
        return profileRepository.findAll();
    }

    public void addProfile(Profile profile){
        profileRepository.save(profile);
    }

    public void deleteProfile(Integer id){
        Profile profile = profileRepository.findProfileById(id);
        if(profile == null){
            throw new ApiException("profile not found");
        }
        profileRepository.delete(profile);
    }
}
