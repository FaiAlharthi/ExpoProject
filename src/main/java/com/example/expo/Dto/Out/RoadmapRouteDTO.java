package com.example.expo.Dto.Out;

import com.example.expo.Model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapRouteDTO {
    private List<String> visitOrder;   // أسماء الأجنحة بالترتيب
    private Double totalDistance;       // مجموع المشي
    private List<Location> route;       // كل النقاط اللي يمشي عليها
    private String exitEntrance;       // البوابة اللي يطلع منها
}
