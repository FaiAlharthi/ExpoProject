package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Passport;
import com.example.expo.Repository.PassportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassportService {
    private final PassportRepository passportRepository;

    public List<Passport> getAllPassports(){
        return passportRepository.findAll();
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
