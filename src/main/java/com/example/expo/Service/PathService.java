package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Dto.RoadmapRouteDTO;
import com.example.expo.Dto.ShortestPathDTO;
import com.example.expo.Model.Location;
import com.example.expo.Model.Path;
import com.example.expo.Model.Pavilion;
import com.example.expo.Model.Roadmap;
import com.example.expo.Model.Visit;
import com.example.expo.Repository.LocationRepository;
import com.example.expo.Repository.PathRepository;
import com.example.expo.Repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.expo.Model.Entrance;
import com.example.expo.Repository.EntranceRepository;


@Service
@RequiredArgsConstructor
public class PathService {
    private final PathRepository pathRepository;
    private final LocationRepository locationRepository;
    private final RoadmapRepository roadmapRepository;
    private final EntranceRepository entranceRepository;

    public List<Path> getAllPaths(){
        return pathRepository.findAll();
    }

    // يضيف ممر بين نقطتين، والمسافة تنحسب تلقائي
    public void addPath(Integer fromId, Integer toId){
        if(fromId.equals(toId)){
            throw new ApiException("Path must connect two different locations");
        }
        Location from = locationRepository.findLocationById(fromId);
        Location to = locationRepository.findLocationById(toId);
        if(from == null || to == null){
            throw new ApiException("Location not found");
        }
        if(pathRepository.findPathByFromLocationAndToLocation(from, to) != null
                || pathRepository.findPathByFromLocationAndToLocation(to, from) != null){
            throw new ApiException("Path already exists");
        }
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();

        Path path = new Path();
        path.setFromLocation(from);
        path.setToLocation(to);
        path.setDistance(Math.sqrt(dx * dx + dy * dy));
        pathRepository.save(path);
    }

    public void deletePath(Integer id){
        Path path = pathRepository.findPathById(id);
        if(path == null){
            throw new ApiException("Path not found");
        }
        pathRepository.delete(path);
    }

    // ===== Dijkstra: أقصر طريق على الممرات بين نقطتين =====
    public ShortestPathDTO getShortestPath(Integer fromId, Integer toId){
        Location start = locationRepository.findLocationById(fromId);
        Location end = locationRepository.findLocationById(toId);
        if(start == null || end == null){
            throw new ApiException("Location not found");
        }

        List<Path> paths = pathRepository.findAll();
        Map<Integer, Double> distance = new HashMap<>();   // أقصر مسافة معروفة لكل نقطة
        Map<Integer, Integer> previous = new HashMap<>();  // جيت لهذي النقطة من وين
        List<Integer> unvisited = new ArrayList<>();

        // 1) كل النقاط بعيدة جداً، إلا البداية = 0
        for(Location location : locationRepository.findAll()){
            distance.put(location.getId(), Double.MAX_VALUE);
            unvisited.add(location.getId());
        }
        distance.put(start.getId(), 0.0);

        while(!unvisited.isEmpty()){
            // 2) خذ أقرب نقطة ما زرناها
            Integer current = null;
            double min = Double.MAX_VALUE;
            for(Integer id : unvisited){
                if(distance.get(id) < min){
                    min = distance.get(id);
                    current = id;
                }
            }
            if(current == null || current.equals(end.getId())){
                break; // وصلنا، أو الباقي ما ينوصل له
            }
            unvisited.remove(current);

            // 3) حدّث جيرانها
            for(Path path : paths){
                Integer neighbor = null;
                if(path.getFromLocation().getId().equals(current)){
                    neighbor = path.getToLocation().getId();
                } else if(path.getToLocation().getId().equals(current)){
                    neighbor = path.getFromLocation().getId();
                }
                if(neighbor == null || !unvisited.contains(neighbor)){
                    continue;
                }
                double newDistance = distance.get(current) + path.getDistance();
                if(newDistance < distance.get(neighbor)){
                    distance.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                }
            }
        }

        if(distance.get(end.getId()) == Double.MAX_VALUE){
            throw new ApiException("No path between these locations");
        }

        // 4) نرجع من النهاية للبداية عشان نبني الطريق
        List<Location> route = new ArrayList<>();
        Integer step = end.getId();
        while(step != null){
            route.add(0, locationRepository.findLocationById(step));
            step = previous.get(step);
        }
        return new ShortestPathDTO(distance.get(end.getId()), route);
    }

    // ===== مسار الرحلة من نقطة يحددها الزائر (مثلاً مكانه الحين) =====
    public RoadmapRouteDTO getRoadmapRoute(Integer roadmapId, Integer startLocationId){
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if(roadmap == null){
            throw new ApiException("Roadmap not found");
        }
        Location start = locationRepository.findLocationById(startLocationId);
        if(start == null){
            throw new ApiException("Start location not found");
        }
        return buildRoute(roadmap, start);
    }

    // ===== مسار الرحلة من بوابة الدخول اللي اختارها الزائر =====
    public RoadmapRouteDTO getRoadmapRouteFromEntrance(Integer roadmapId){
        Roadmap roadmap = roadmapRepository.findRoadmapById(roadmapId);
        if(roadmap == null){
            throw new ApiException("Roadmap not found");
        }
        if(roadmap.getEntrance() == null || roadmap.getEntrance().getLocation() == null){
            throw new ApiException("Roadmap entrance has no location");
        }
        return buildRoute(roadmap, roadmap.getEntrance().getLocation());
    }

    // يرتب الأجنحة اللي ما انزارت: كل مرة أقرب جناح باقي بمسافة Dijkstra
    private RoadmapRouteDTO buildRoute(Roadmap roadmap, Location start){
        List<Pavilion> remaining = new ArrayList<>();
        if(roadmap.getVisits() != null){
            for(Visit visit : roadmap.getVisits()){
                if(!visit.getIsVisited() && visit.getPavilion() != null && visit.getPavilion().getLocation() != null){
                    remaining.add(visit.getPavilion());
                }
            }
        }
        if(remaining.isEmpty()){
            throw new ApiException("Roadmap has no remaining pavilions");
        }

        List<String> visitOrder = new ArrayList<>();
        List<Location> fullRoute = new ArrayList<>();
        double totalDistance = 0;

        Location current = start;
        fullRoute.add(current);

        while(!remaining.isEmpty()){
            // 1) أقرب جناح باقي
            Pavilion nextPavilion = null;
            ShortestPathDTO bestPath = null;

            for(Pavilion pavilion : remaining){
                ShortestPathDTO path;
                try {
                    path = getShortestPath(current.getId(), pavilion.getLocation().getId());
                } catch (ApiException e) {
                    continue; // ما فيه ممر يوصل له
                }
                if(bestPath == null || path.getTotalDistance() < bestPath.getTotalDistance()){
                    bestPath = path;
                    nextPavilion = pavilion;
                }
            }

            if(nextPavilion == null){
                throw new ApiException("Some pavilions are not connected by paths");
            }

            // 2) نضيف طريقه للمسار الكامل (بدون تكرار نقطة البداية)
            for(int i = 1; i < bestPath.getLocations().size(); i++){
                fullRoute.add(bestPath.getLocations().get(i));
            }
            totalDistance = totalDistance + bestPath.getTotalDistance();
            visitOrder.add(nextPavilion.getName());

            // 3) الجناح هذا يصير نقطة البداية الجديدة
            current = nextPavilion.getLocation();
            remaining.remove(nextPavilion);
        }

        // 4) بعد آخر جناح: نطلع من أقرب بوابة
        String exitEntrance = null;
        ShortestPathDTO exitPath = null;
        for(Entrance entrance : entranceRepository.findAll()){
            if(entrance.getLocation() == null){
                continue;
            }
            ShortestPathDTO path;
            try {
                path = getShortestPath(current.getId(), entrance.getLocation().getId());
            } catch (ApiException e) {
                continue;
            }
            if(exitPath == null || path.getTotalDistance() < exitPath.getTotalDistance()){
                exitPath = path;
                exitEntrance = entrance.getName();
            }
        }
        if(exitPath != null){
            for(int i = 1; i < exitPath.getLocations().size(); i++){
                fullRoute.add(exitPath.getLocations().get(i));
            }
            totalDistance = totalDistance + exitPath.getTotalDistance();
        }

        return new RoadmapRouteDTO(visitOrder, totalDistance, fullRoute, exitEntrance);
    }
}