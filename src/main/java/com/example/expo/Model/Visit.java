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
    //كل صف = جواز معيّن زار جناح معيّن في وقت معيّن
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //يتعبى تلقائي في السيرفس
    @Column(columnDefinition = "DATETIME not null")
    private LocalDateTime visitedAt;

    @Column(columnDefinition = "BOOLEAN default false")
    private Boolean isLiked = false;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Passport passport;

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
