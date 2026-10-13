package com.example.expo.Config;

import com.example.expo.Model.Entrance;
import com.example.expo.Model.Location;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Zone;
import com.example.expo.Repository.EntranceRepository;
import com.example.expo.Repository.LocationRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ZoneRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// fills the database from data/expo-data.json on startup, only if it is empty
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final EntranceRepository entranceRepository;
    private final PavilionRepository pavilionRepository;

    @Override
    public void run(String... args) throws Exception {
        if(zoneRepository.count() > 0 || entranceRepository.count() > 0 || pavilionRepository.count() > 0){
            return;
        }

        InputStream file = new ClassPathResource("data/expo-data.json").getInputStream();
        SeedData data = new ObjectMapper().readValue(file, SeedData.class);

        Map<String, Zone> zones = new HashMap<>();
        for(SeedZone seedZone : data.getZones()){
            Zone zone = new Zone();
            zone.setName(seedZone.getName());
            zone.setDescription(seedZone.getDescription());
            zone.setLocation(saveLocation(seedZone.getLocation()));
            zoneRepository.save(zone);
            zones.put(zone.getName(), zone);
        }

        for(SeedEntrance seedEntrance : data.getEntrances()){
            Entrance entrance = new Entrance();
            entrance.setName(seedEntrance.getName());
            entrance.setLocation(saveLocation(seedEntrance.getLocation()));
            entranceRepository.save(entrance);
        }

        for(SeedPavilion seedPavilion : data.getPavilions()){
            Pavilion pavilion = new Pavilion();
            pavilion.setName(seedPavilion.getName());
            pavilion.setCountry(seedPavilion.getCountry());
            pavilion.setDescription(seedPavilion.getDescription());
            pavilion.setImageUrl(seedPavilion.getImageUrl());
            pavilion.setZone(zones.get(seedPavilion.getZone()));
            pavilion.setLocation(saveLocation(seedPavilion.getLocation()));
            pavilionRepository.save(pavilion);
        }
    }

    private Location saveLocation(SeedLocation seedLocation){
        Location location = new Location();
        location.setX(seedLocation.getX());
        location.setY(seedLocation.getY());
        return locationRepository.save(location);
    }

    // shapes of the json file
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedData {
        private List<SeedZone> zones;
        private List<SeedEntrance> entrances;
        private List<SeedPavilion> pavilions;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedLocation {
        private Double x;
        private Double y;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedZone {
        private String name;
        private String description;
        private SeedLocation location;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedEntrance {
        private String name;
        private SeedLocation location;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeedPavilion {
        private String name;
        private String country;
        private String description;
        private String imageUrl;
        private String zone;
        private SeedLocation location;
    }
}
