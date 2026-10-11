package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Interest;
import com.example.expo.Model.Profile;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.InterestRepository;
import com.example.expo.Repository.ProfileRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorService {
    private final VisitorRepository visitorRepository;
    private final ProfileRepository profileRepository;
    private final InterestRepository interestRepository;

    public List<Visitor> getAllVisitors(){
        return visitorRepository.findAll();
    }

    public void addVisitor(Integer profileId, Visitor visitor){
        Profile profile = profileRepository.findProfileById(profileId);
        if(profile == null){
            throw new ApiException("profile not found");
        }
        if(visitorRepository.findVisitorById(profileId) != null){
            throw new ApiException("this profile already has visitor");
        }
        visitor.setProfile(profile);
        visitorRepository.save(visitor);
    }

    public void deleteVisitor(Integer id){
        Visitor visitor = visitorRepository.findVisitorById(id);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        visitor.getProfile().setVisitor(null);
        visitorRepository.delete(visitor);
    }

    public void addInterestToVisitor(Integer visitorId, Integer interestId){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Interest interest = interestRepository.findInterestById(interestId);
        if(interest == null){
            throw new ApiException("interest not found");
        }
        visitor.getInterests().add(interest);
        visitorRepository.save(visitor);
    }
}
