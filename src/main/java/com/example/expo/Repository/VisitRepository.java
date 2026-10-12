package com.example.expo.Repository;

import com.example.expo.Model.Passport;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {
    Visit findVisitById(Integer id);

    //يتأكد اذا الجواز زار نفس الجناح في نفس اليوم
    Boolean existsByPassportAndPavilionAndVisitedAtBetween(Passport passport, Pavilion pavilion, LocalDateTime start, LocalDateTime end);

    Long countByPassport(Passport passport);
}
