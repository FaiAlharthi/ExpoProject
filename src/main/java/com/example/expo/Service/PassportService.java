package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Passport;
import com.example.expo.Model.Visitor;
import com.example.expo.Repository.PassportRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassportService {
    private final PassportRepository passportRepository;
    private final VisitorRepository visitorRepository;

    public List<Passport> getAllPassports(){
        return passportRepository.findAll();
    }

    public void addPassport(Integer visitorId, Passport passport){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        if(visitor.getPassport() != null){
            throw new ApiException("this visitor already has passport");
        }
        // passport number from visitor id
        passport.setPassportNumber(String.format("EXPO30-%06d", visitor.getId()));
        passport.setExplorerLevel("BRONZE");
        passport.setVisitor(visitor);
        passportRepository.save(passport);
    }

    public void deletePassport(Integer id){
        Passport passport = passportRepository.findPassportById(id);
        if(passport == null){
            throw new ApiException("passport not found");
        }
        passport.getVisitor().setPassport(null);
        passportRepository.delete(passport);
    }
}
