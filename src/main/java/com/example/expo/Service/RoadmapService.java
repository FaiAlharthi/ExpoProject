package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Dto.In.GenerateRoadmapDTO;
import com.example.expo.Dto.Out.RoadmapBreakDTO;
import com.example.expo.Dto.Out.RoadmapDTO;
import com.example.expo.Dto.Out.RoadmapStopDTO;
import com.example.expo.Model.Entrance;
import com.example.expo.Model.Location;
import com.example.expo.Model.Interest;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Review;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Model.Visitor;
import com.example.expo.Model.Zone;
import com.example.expo.Repository.EntranceRepository;
import com.example.expo.Repository.InterestRepository;
import com.example.expo.Repository.PavilionRepository;
import com.example.expo.Repository.ReviewRepository;
import com.example.expo.Repository.RoadmapRepository;
import com.example.expo.Repository.VisitRepository;
import com.example.expo.Repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final GeminiService geminiService;
    private final ReviewRepository reviewRepository;
    private final InterestRepository interestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public RoadmapDTO generateRoadmap(Integer visitorId, Integer entranceId, GenerateRoadmapDTO dto){
        Visitor visitor = visitorRepository.findVisitorById(visitorId);
        if(visitor == null){
            throw new ApiException("visitor not found");
        }
        Entrance entrance = entranceRepository.findEntranceById(entranceId);
        if(entrance == null){
            throw new ApiException("entrance not found");
        }
        if(visitor.getInterests() == null || visitor.getInterests().size() < 3){
            throw new ApiException("add at least 3 interests first");
        }
        Set<Interest> extraInterests = new HashSet<>();
        if(dto.getExtraInterestIds() != null){
            for(Integer interestId : dto.getExtraInterestIds()){
                Interest interest = interestRepository.findInterestById(interestId);
                if(interest == null){
                    throw new ApiException("interest not found");
                }
                extraInterests.add(interest);
            }
        }
        Roadmap answers = toRoadmap(dto);
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
        roadmap.setExtraInterests(extraInterests);
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

        // AI chooses the pavilions, Dijkstra orders them
        List<Pavilion> ordered;
        boolean aiUsed = false;
        roadmap.setAiBreaks(null);
        roadmap.setAiSummary(null);
        try{
            Map<?, ?> aiAnswer = objectMapper.readValue(geminiService.ask(buildPrompt(visitor, roadmap, entrance, candidates)), Map.class);
            List<Pavilion> selected = readSelectedPavilions(aiAnswer, candidates);
            if(selected.isEmpty()){
                throw new ApiException("AI selected no pavilions");
            }
            ordered = orderPavilions(entrance.getLocation(), selected, selected.size());
            roadmap.setAiBreaks(aiAnswer.get("breaks") == null ? null : objectMapper.writeValueAsString(aiAnswer.get("breaks")));
            roadmap.setAiSummary(aiAnswer.get("summary") == null ? null : aiAnswer.get("summary").toString());
            aiUsed = true;
        }catch(Exception e){
            // AI failed, use the nearest pavilions
            System.out.println("AI roadmap failed: " + e.getMessage());
            ordered = orderPavilions(entrance.getLocation(), candidates, maxStops);
        }
        roadmapRepository.save(roadmap);

        saveStops(roadmap, ordered, lastOrder);
        RoadmapDTO result = toRoadmapDTO(roadmap);
        result.setAiUsed(aiUsed);
        return result;
    }

    public RoadmapDTO getStops(Integer roadmapId){
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if(roadmap == null){
            throw new ApiException("roadmap not found");
        }
        return toRoadmapDTO(roadmap);
    }

    private RoadmapDTO toRoadmapDTO(Roadmap roadmap){
        List<RoadmapBreakDTO> breaks = new ArrayList<>();
        if(roadmap.getAiBreaks() != null){
            try{
                for(Object item : objectMapper.readValue(roadmap.getAiBreaks(), List.class)){
                    Map<?, ?> b = (Map<?, ?>) item;
                    breaks.add(new RoadmapBreakDTO(
                            b.get("type") == null ? null : b.get("type").toString(),
                            b.get("time") == null ? null : b.get("time").toString(),
                            b.get("minutes") instanceof Number n ? n.intValue() : null,
                            b.get("note") == null ? null : b.get("note").toString()));
                }
            }catch(Exception e){
                breaks = new ArrayList<>();
            }
        }
        List<RoadmapStopDTO> stops = new ArrayList<>();
        for(Visit visit : visitRepository.findVisitsByRoadmapOrderByStopOrderAsc(roadmap)){
            Pavilion pavilion = visit.getPavilion();
            stops.add(new RoadmapStopDTO(visit.getId(), visit.getStopOrder(), pavilion.getId(), pavilion.getName(),
                    pavilion.getCountry(), pavilion.getZone() == null ? null : pavilion.getZone().getName(),
                    pavilion.getImageUrl(), visit.getIsVisited(), visit.getVisitedAt()));
        }
        // a summary means the AI made this roadmap
        return new RoadmapDTO(roadmap.getId(), roadmap.getVisitDate(), roadmap.getPlanType(), roadmap.getAiSummary(),
                roadmap.getAiSummary() != null, breaks, stops);
    }

    private Roadmap toRoadmap(GenerateRoadmapDTO dto){
        Roadmap roadmap = new Roadmap();
        roadmap.setPlanType(dto.getPlanType());
        roadmap.setVisitDate(dto.getVisitDate());
        roadmap.setPartyType(dto.getPartyType());
        roadmap.setPartySize(dto.getPartySize());
        roadmap.setArrivalTime(dto.getArrivalTime());
        roadmap.setDepartureTime(dto.getDepartureTime());
        roadmap.setExperienceTypes(dto.getExperienceTypes());
        roadmap.setVisitPace(dto.getVisitPace());
        roadmap.setIncludePrayerBreaks(dto.getIncludePrayerBreaks() != null && dto.getIncludePrayerBreaks());
        roadmap.setIncludeMealBreaks(dto.getIncludeMealBreaks() != null && dto.getIncludeMealBreaks());
        roadmap.setIncludeChildRestBreaks(dto.getIncludeChildRestBreaks() != null && dto.getIncludeChildRestBreaks());
        roadmap.setAllowRevisits(dto.getAllowRevisits());
        return roadmap;
    }

    // everything the AI needs to choose the pavilions
    private String buildPrompt(Visitor visitor, Roadmap roadmap, Entrance entrance, List<Pavilion> candidates){
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are the visit planner of Expo 2030 Riyadh. Choose the best pavilions for this visitor for one day.\n\n");

        prompt.append("VISITOR\n");
        prompt.append("age: ").append(visitor.getAge()).append(", gender: ").append(visitor.getGender())
                .append(", nationality: ").append(visitor.getNationality())
                .append(", language: ").append(visitor.getPreferredLanguage()).append("\n");
        List<String> interests = new ArrayList<>();
        if(visitor.getInterests() != null){
            for(Interest interest : visitor.getInterests()){
                interests.add(interest.getName());
            }
        }
        prompt.append("interests: ").append(interests).append("\n");
        List<String> extra = new ArrayList<>();
        if(roadmap.getExtraInterests() != null){
            for(Interest interest : roadmap.getExtraInterests()){
                extra.add(interest.getName());
            }
        }
        prompt.append("extra interests for today: ").append(extra.isEmpty() ? "none" : extra).append("\n\n");

        prompt.append("TODAY\n");
        prompt.append("date: ").append(roadmap.getVisitDate())
                .append(", arrival: ").append(roadmap.getArrivalTime())
                .append(", departure: ").append(roadmap.getDepartureTime()).append("\n");
        prompt.append("party: ").append(roadmap.getPartyType()).append(", size: ").append(roadmap.getPartySize()).append("\n");
        prompt.append("mood: ").append(roadmap.getExperienceTypes()).append(", pace: ").append(roadmap.getVisitPace()).append("\n");
        prompt.append("breaks wanted: prayer=").append(roadmap.getIncludePrayerBreaks())
                .append(", meal=").append(roadmap.getIncludeMealBreaks())
                .append(", child rest=").append(roadmap.getIncludeChildRestBreaks()).append("\n");
        prompt.append("entrance: ").append(entrance.getName()).append(" at (")
                .append(entrance.getLocation().getX()).append(", ").append(entrance.getLocation().getY()).append(")\n\n");

        prompt.append("PREVIOUS REVIEWS (rating 1-5)\n");
        boolean hasReviews = false;
        for(Review review : reviewRepository.findReviewsByVisitor(visitor)){
            if(review.getCreatedAt() != null && review.getCreatedAt().toLocalDate().isBefore(roadmap.getVisitDate())){
                prompt.append("- ").append(review.getPavilion().getName()).append(": ").append(review.getRating());
                if(review.getComment() != null){
                    prompt.append(" \"").append(review.getComment()).append("\"");
                }
                prompt.append("\n");
                hasReviews = true;
            }
        }
        if(!hasReviews){
            prompt.append("none\n");
        }

        prompt.append("\nAVAILABLE PAVILIONS (id | name | country | zone | description)\n");
        for(Pavilion pavilion : candidates){
            String description = pavilion.getDescription() == null ? "" : pavilion.getDescription();
            if(description.length() > 200){
                description = description.substring(0, 200);
            }
            prompt.append(pavilion.getId()).append(" | ").append(pavilion.getName()).append(" | ")
                    .append(pavilion.getCountry()).append(" | ").append(pavilion.getZone().getName())
                    .append(" | ").append(description).append("\n");
        }

        prompt.append("\nRULES\n");
        prompt.append("- Choose only ids from the list above.\n");
        prompt.append("- Match the interests, the mood, the party (children, family, solo) and the previous reviews (like what was rated high, avoid what was rated low).\n");
        prompt.append("- Estimate a realistic time for each pavilion and the walking between them. All chosen pavilions and the breaks must fit between arrival and departure. If time is short, choose the most important ones. RELAXED pace means fewer pavilions.\n");
        prompt.append("- Add only the breaks that are wanted. Prayer breaks use the prayer times in Riyadh for that date.\n");
        prompt.append("- Do not order the pavilions by distance, the system does that.\n");
        prompt.append("- Write the summary in Arabic if language is AR, otherwise in English.\n\n");

        prompt.append("Answer with JSON only in this shape:\n");
        prompt.append("{\"pavilionIds\": [1, 2], \"breaks\": [{\"type\": \"PRAYER|MEAL|CHILD_REST\", \"time\": \"HH:mm\", \"minutes\": 20, \"note\": \"short note\"}], \"summary\": \"one or two sentences about the plan\"}");
        return prompt.toString();
    }

    // keeps only valid ids from the AI answer, without duplicates
    private List<Pavilion> readSelectedPavilions(Map<?, ?> aiAnswer, List<Pavilion> candidates){
        Map<Integer, Pavilion> available = new HashMap<>();
        for(Pavilion pavilion : candidates){
            available.put(pavilion.getId(), pavilion);
        }
        List<Pavilion> selected = new ArrayList<>();
        if(aiAnswer.get("pavilionIds") instanceof List<?> ids){
            for(Object id : ids){
                if(id instanceof Number number){
                    Pavilion pavilion = available.remove(number.intValue());
                    if(pavilion != null){
                        selected.add(pavilion);
                    }
                }
            }
        }
        return selected;
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

    // zone distance is measured to its center, or to its first pavilion if it has no location
    private Zone nearestZone(Location from, List<Pavilion> pavilions){
        Zone nearest = null;
        double best = Double.MAX_VALUE;
        Set<Integer> checked = new HashSet<>();
        for(Pavilion pavilion : pavilions){
            Zone zone = pavilion.getZone();
            if(!checked.add(zone.getId())){
                continue;
            }
            Location target = zone.getLocation() != null ? zone.getLocation() : pavilion.getLocation();
            double distance = getDistance(from, target);
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
            double distance = getDistance(from, pavilion.getLocation());
            if(distance < best){
                best = distance;
                nearest = pavilion;
            }
        }
        return nearest;
    }

    // walking distance from Dijkstra, straight line if there is no path yet
    private double getDistance(Location from, Location to){
        try{
            return pathService.getShortestPath(from.getId(), to.getId()).getTotalDistance();
        }catch(ApiException e){
            return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
        }
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
