package com.example.expo.Dto.Out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapStopResponse {
    private Integer visitId;
    private Integer stopOrder;
    private Integer pavilionId;
    private String pavilionName;
    private String country;
    private String zoneName;
    private String imageUrl;
    private Boolean isVisited;
    private LocalDateTime visitedAt;
    private List<AccessibilityWarningResponse> accessibilityWarnings;
}
