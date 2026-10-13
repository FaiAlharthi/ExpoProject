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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "plan type cannot be empty")
    @Pattern(regexp = "^(STATIC|DYNAMIC)$", message = "plan type must be STATIC or DYNAMIC")
    @Column(columnDefinition = "VARCHAR(7) not null")
    private String planType;

    // checked in service
    @NotNull(message = "visit date cannot be empty")
    @Column(columnDefinition = "DATE not null")
    private LocalDate visitDate;

    @NotEmpty(message = "party type cannot be empty")
    @Pattern(regexp = "^(SOLO|COUPLE|FAMILY_WITH_CHILDREN|FRIENDS|GROUP)$", message = "party type must be SOLO, COUPLE, FAMILY_WITH_CHILDREN, FRIENDS or GROUP")
    @Column(columnDefinition = "VARCHAR(20) not null")
    private String partyType;

    @NotNull(message = "party size cannot be empty")
    @Min(value = 1, message = "party size must be at least 1")
    @Column(columnDefinition = "INTEGER not null")
    private Integer partySize;

    @NotNull(message = "arrival time cannot be empty")
    @Column(columnDefinition = "TIME not null")
    private LocalTime arrivalTime;

    // checked in service
    @NotNull(message = "departure time cannot be empty")
    @Column(columnDefinition = "TIME not null")
    private LocalTime departureTime;

    @NotEmpty(message = "experience types cannot be empty")
    @ElementCollection
    private Set<@Pattern(regexp = "^(ENTERTAINMENT|CULTURAL|EDUCATIONAL|RELAXATION|CULINARY)$", message = "experience type must be ENTERTAINMENT, CULTURAL, EDUCATIONAL, RELAXATION or CULINARY") String> experienceTypes;

    @NotEmpty(message = "visit pace cannot be empty")
    @Pattern(regexp = "^(RELAXED|INTENSIVE)$", message = "visit pace must be RELAXED or INTENSIVE")
    @Column(columnDefinition = "VARCHAR(9) not null")
    private String visitPace;

    @Column(columnDefinition = "BOOLEAN default false")
    private Boolean includePrayerBreaks = false;

    @Column(columnDefinition = "BOOLEAN default false")
    private Boolean includeMealBreaks = false;

    @Column(columnDefinition = "BOOLEAN default false")
    private Boolean includeChildRestBreaks = false;

    // true = can include visited pavilions
    @NotNull(message = "allow revisits cannot be empty")
    @Column(columnDefinition = "BOOLEAN not null")
    private Boolean allowRevisits;

    // set in service
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;

    // the entrance the visitor starts from
    @ManyToOne
    @JoinColumn
    private Entrance entrance;

    // roadmap stops
    @OneToMany(mappedBy = "roadmap")
    private Set<Visit> visits;
}
