package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // null until check-in
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime visitedAt;

    @Column(columnDefinition = "BOOLEAN default false")
    private Boolean isVisited = false;

    // order in the roadmap, null if not in a roadmap
    @Column(columnDefinition = "INTEGER")
    private Integer stopOrder;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Passport passport;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Roadmap roadmap;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Pavilion pavilion;


    // يرجع اسم الجناح في الـ JSON بدل الجناح كامل لان عليه جاسون اقنور ال بافلين فيصير نحط اسمه بس
    @JsonProperty("pavilionName")
    public String getPavilionName() {
        if (pavilion == null) {
            return null;
        }
        return pavilion.getName();
    }

}
