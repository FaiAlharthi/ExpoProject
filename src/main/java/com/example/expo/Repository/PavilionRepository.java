package com.example.expo.Repository;

import com.example.expo.Model.Pavilion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PavilionRepository extends JpaRepository<Pavilion, Integer> {
    Pavilion findPavilionById(Integer id);
    Pavilion findPavilionByName(String name);
}
