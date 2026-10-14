package com.example.expo.Dto.Out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// all admin statistics in one response
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    private Long totalVisitors;
    private Long totalCheckIns;
    private Long totalRoadmaps;
    private List<PavilionStatResponse> mostVisitedPavilions;
    private List<PavilionStatResponse> topRatedPavilions;
    private List<PavilionStatResponse> lowestRatedPavilions;
    private List<CountResponse> topInterests;
    private List<CountResponse> visitsPerDay;
    private List<CountResponse> visitsPerZone;
    private List<CountResponse> entrances;
}
