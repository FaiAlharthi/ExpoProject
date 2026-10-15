package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Roadmap {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Visit date cannot be empty!")
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate visitDate; // Checked in service

    @NotEmpty(message = "Party type cannot be empty!")
    @Pattern(regexp = "^(SOLO|GROUP)$", message = "Party type must be SOLO or GROUP.")
    @Column(columnDefinition = "VARCHAR(5) NOT NULL")
    private String partyType;

    @NotNull(message = "Party size cannot be empty!")
    @Min(value = 1, message = "Party size must be at least 1")
    @Column(columnDefinition = "INTEGER NOT NULL")
    private Integer partySize;

    @NotNull(message = "Arrival time cannot be empty!")
    @Column(columnDefinition = "TIME NOT NULL")
    private LocalTime arrivalTime;

    // checked in service
    @NotNull(message = "Departure time cannot be empty!")
    @Column(columnDefinition = "TIME NOT NULL")
    private LocalTime departureTime;

    @NotEmpty(message = "Experience types cannot be empty!")
    @ElementCollection
    private Set<@Pattern(regexp = "^(ENTERTAINMENT|CULTURAL|EDUCATIONAL|RELAXATION|CULINARY)$",
            message = "Experience type must be ENTERTAINMENT, CULTURAL, EDUCATIONAL, RELAXATION, or CULINARY.")
            String> experienceTypes;

    @NotEmpty(message = "Visit pace cannot be empty!")
    @Pattern(regexp = "^(RELAXED|INTENSIVE)$", message = "Visit pace must be RELAXED or INTENSIVE.")
    @Column(columnDefinition = "VARCHAR(9) NOT NULL")
    private String visitPace;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean includePrayerBreaks = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean includeMealBreaks = false;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean includeChildRestBreaks = false;

    @NotNull(message = "Allow revisits cannot be empty!")
    @Column(columnDefinition = "BOOLEAN NOT NULL")
    private Boolean allowRevisits; // True == can include visited pavilions

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt; // Set in service

    @Column(columnDefinition = "TEXT")
    private String aiBreaks; // Set by AI, breaks are saved as json text.

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    //Relations
    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;

    @ManyToOne
    @JoinColumn
    private Entrance entrance; // The entrance the visitor starts from

    @ManyToMany
    private Set<Interest> extraInterests; // Roadmap stops, optional interests added for this day ONLY.

    @OneToMany(mappedBy = "roadmap")
    private Set<Visit> visits;
}
