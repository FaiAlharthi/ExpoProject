package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.Entrance;
import com.example.expo.Model.Location;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Model.Zone;
import com.example.expo.Repository.EntranceRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoadmapService {
    private final RoadmapRepository roadmapRepository;
    private final VisitorRepository visitorRepository;
    private final VisitRepository visitRepository;
    private final EntranceRepository entranceRepository;
    private final PavilionRepository pavilionRepository;
    private final PathService pathService;

    // average minutes per pavilion, temporary until the AI is added
    private static final int MINUTES_PER_PAVILION = 30;

    public List<Roadmap> getAllRoadmaps(){
        return roadmapRepository.findAll();
    }

    // manual roadmap without generating stops
    public void addRoadmap(Integer visitorId, Integer entranceId, Roadmap roadmap){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Entrance entrance = entranceRepository.findEntranceById(entranceId);
        if(entrance == null){
            throw new ApiException("entrance not found");
        }
        checkDateAndTime(roadmap);
        if(roadmapRepository.findRoadmapByVisitorAndVisitDate(visitor, roadmap.getVisitDate()) != null){
            throw new ApiException("you already have a roadmap for this day");
        }
        roadmap.setCreatedAt(LocalDateTime.now());
        roadmap.setVisitor(visitor);
        roadmap.setEntrance(entrance);
        roadmapRepository.save(roadmap);
    }

    // creates the day roadmap, or replaces it if it already exists
    public List<Visit> generateRoadmap(Integer visitorId, Integer entranceId, Roadmap answers){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Entrance entrance = entranceRepository.findEntranceById(entranceId);
        if(entrance == null){
            throw new ApiException("entrance not found");
        }
        checkDateAndTime(answers);
        if(answers.getVisitDate().isBefore(LocalDate.now())){
            throw new ApiException("cannot generate a roadmap for a past day");
        }

        Roadmap roadmap = roadmapRepository.findRoadmapByVisitorAndVisitDate(visitor, answers.getVisitDate());
        if(roadmap == null){
            roadmap = answers;
            roadmap.setVisitor(visitor);
            roadmap.setCreatedAt(LocalDateTime.now());
        }else{
            copyAnswers(answers, roadmap);
            // only the latest version counts, visited stops stay
            visitRepository.deleteAll(visitRepository.findVisitsByRoadmapAndIsVisitedFalse(roadmap));
        }
        roadmap.setEntrance(entrance);
        roadmapRepository.save(roadmap);

        int lastOrder = 0;
        for(Visit stop : visitRepository.findVisitsByRoadmapOrderByStopOrderAsc(roadmap)){
            if(stop.getStopOrder() != null && stop.getStopOrder() > lastOrder){
                lastOrder = stop.getStopOrder();
            }
        }

        Set<Integer> visitedToday = new HashSet<>();
        List<Pavilion> candidates = selectPavilions(visitor, roadmap, visitedToday);

        long minutes = Duration.between(roadmap.getArrivalTime(), roadmap.getDepartureTime()).toMinutes();
        int maxStops = (int) (minutes / MINUTES_PER_PAVILION) - visitedToday.size();
        if(maxStops <= 0){
            throw new ApiException("not enough time for more pavilions today");
        }

        List<Pavilion> ordered = orderPavilions(entrance.getLocation(), candidates, maxStops);
        saveStops(roadmap, ordered, lastOrder);
        return visitRepository.findVisitsByRoadmapOrderByStopOrderAsc(roadmap);
    }

    public List<Visit> getStops(Integer roadmapId){
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        return visitRepository.findVisitsByRoadmapOrderByStopOrderAsc(roadmap);
    }

    /*
    public void updateRoadmap(Integer id, Roadmap roadmap){
        Roadmap oldRoadmap = roadmapRepository.findRoadmapById(id);
        if(oldRoadmap == null){
            throw new ApiException("roadmap not found");
        }
        checkDateAndTime(roadmap);
        Roadmap sameDayRoadmap = roadmapRepository.findRoadmapByVisitorAndVisitDate(oldRoadmap.getVisitor(), roadmap.getVisitDate());
        if(sameDayRoadmap != null && !sameDayRoadmap.getId().equals(id)){
            throw new ApiException("you already have a roadmap for this day");
        }
        copyAnswers(roadmap, oldRoadmap);
        roadmapRepository.save(oldRoadmap);
    }
    */

    public void deleteRoadmap(Integer id){
        Roadmap roadmap = roadmapRepository.findRoadmapById(id);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        // keep visits, only unlink them
        if(roadmap.getVisits() != null){
            for(Visit visit : roadmap.getVisits()){
                visit.setRoadmap(null);
                visitRepository.save(visit);
            }
        }
        roadmapRepository.delete(roadmap);
    }

    // removes pavilions visited today, and visited before if revisits are not allowed
    private List<Pavilion> selectPavilions(Visitor visitor, Roadmap roadmap, Set<Integer> visitedToday){
        Set<Integer> excluded = new HashSet<>();
        if(visitor.getPassport() != null){
            for(Visit visit : visitRepository.findVisitsByPassportAndIsVisitedTrue(visitor.getPassport())){
                Integer pavilionId = visit.getPavilion().getId();
                boolean today = visit.getVisitedAt() != null && visit.getVisitedAt().toLocalDate().equals(roadmap.getVisitDate());
                if(today){
                    visitedToday.add(pavilionId);
                    excluded.add(pavilionId);
                }else if(!roadmap.getAllowRevisits()){
                    excluded.add(pavilionId);
                }
            }
        }
        List<Pavilion> candidates = new ArrayList<>();
        for(Pavilion pavilion : pavilionRepository.findAll()){
            if(!excluded.contains(pavilion.getId()) && pavilion.getLocation() != null && pavilion.getZone() != null){
                candidates.add(pavilion);
            }
        }
        return candidates;
    }

    // starts from the nearest zone, finishes it, then moves to the nearest next zone
    private List<Pavilion> orderPavilions(Location start, List<Pavilion> candidates, int maxStops){
        List<Pavilion> ordered = new ArrayList<>();
        List<Pavilion> remaining = new ArrayList<>(candidates);
        Location current = start;
        Zone currentZone = null;

        while(ordered.size() < maxStops && !remaining.isEmpty()){
            List<Pavilion> options = pavilionsInZone(remaining, currentZone);
            if(options.isEmpty()){
                currentZone = nearestZone(current, remaining);
                options = pavilionsInZone(remaining, currentZone);
            }
            Pavilion next = nearestPavilion(current, options);
            ordered.add(next);
            remaining.remove(next);
            current = next.getLocation();
        }
        return ordered;
    }

    private List<Pavilion> pavilionsInZone(List<Pavilion> pavilions, Zone zone){
        List<Pavilion> result = new ArrayList<>();
        if(zone == null){
            return result;
        }
        for(Pavilion pavilion : pavilions){
            if(pavilion.getZone().getId().equals(zone.getId())){
                result.add(pavilion);
            }
        }
        return result;
    }

    // zone distance is measured to its center, or to its nearest pavilion if it has no location
    private Zone nearestZone(Location from, List<Pavilion> pavilions){
        Zone nearest = null;
        double best = Double.MAX_VALUE;
        for(Pavilion pavilion : pavilions){
            Zone zone = pavilion.getZone();
            Location target = zone.getLocation() != null ? zone.getLocation() : pavilion.getLocation();
            double distance = pathService.getWalkingDistance(from, target);
            if(distance < best){
                best = distance;
                nearest = zone;
            }
        }
        return nearest;
    }

    private Pavilion nearestPavilion(Location from, List<Pavilion> pavilions){
        Pavilion nearest = null;
        double best = Double.MAX_VALUE;
        for(Pavilion pavilion : pavilions){
            double distance = pathService.getWalkingDistance(from, pavilion.getLocation());
            if(distance < best){
                best = distance;
                nearest = pavilion;
            }
        }
        return nearest;
    }

    private void saveStops(Roadmap roadmap, List<Pavilion> pavilions, int lastOrder){
        int order = lastOrder;
        for(Pavilion pavilion : pavilions){
            Visit stop = new Visit();
            stop.setRoadmap(roadmap);
            stop.setPavilion(pavilion);
            stop.setStopOrder(++order);
            stop.setIsVisited(false);
            visitRepository.save(stop);
        }
    }

    private void copyAnswers(Roadmap from, Roadmap to){
        to.setPlanType(from.getPlanType());
        to.setVisitDate(from.getVisitDate());
        to.setPartyType(from.getPartyType());
        to.setPartySize(from.getPartySize());
        to.setArrivalTime(from.getArrivalTime());
        to.setDepartureTime(from.getDepartureTime());
        to.setExperienceTypes(from.getExperienceTypes());
        to.setVisitPace(from.getVisitPace());
        to.setIncludePrayerBreaks(from.getIncludePrayerBreaks());
        to.setIncludeMealBreaks(from.getIncludeMealBreaks());
        to.setIncludeChildRestBreaks(from.getIncludeChildRestBreaks());
        to.setAllowRevisits(from.getAllowRevisits());
    }

    // date must be in expo period, departure after arrival
    private void checkDateAndTime(Roadmap roadmap){
        if(roadmap.getVisitDate().isBefore(LocalDate.of(2030, 10, 1)) || roadmap.getVisitDate().isAfter(LocalDate.of(2031, 3, 31))){
            throw new ApiException("visit date must be between 2030-10-01 and 2031-03-31");
        }
        if(!roadmap.getDepartureTime().isAfter(roadmap.getArrivalTime())){
            throw new ApiException("departure time must be after arrival time");
        }
    }
}
