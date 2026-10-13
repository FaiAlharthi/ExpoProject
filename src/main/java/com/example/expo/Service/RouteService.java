package com.example.expo.Service;

import com.example.expo.Model.Location;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    // straight line distance for now, will be replaced by Dijkstra
    public Double getDistance(Location from, Location to){
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2));
    }
}
