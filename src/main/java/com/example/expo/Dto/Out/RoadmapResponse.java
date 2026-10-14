package com.example.expo.Dto.Out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapResponse {
    private Integer roadmapId;
    private LocalDate visitDate;
    private String planType;
    private String summary;
    private Boolean aiUsed;
    private List<RoadmapBreakResponse> breaks;
    private List<RoadmapStopResponse> stops;
}
