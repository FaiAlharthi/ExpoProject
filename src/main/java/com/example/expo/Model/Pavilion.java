package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Pavilion {
    @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

 @NotEmpty(message = "name should not be empty")
 @Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
 @Column(nullable = false, unique = true)
 private String name;//unique

    @NotEmpty(message = "description should not be empty")
 @Size(max = 1000, message = "description must be less than 1000 characters")
 @Column(length = 1000,nullable = false)
 private String description;

// @NotNull(message = "indoor should not be empty")
// @Column(nullable = false)
// private Boolean indoor; //for weather api  //مفتوح مغلق


// @NotNull(message = "visit duration should not be empty")
// @Min(value = 5, message = "visit duration must be at least 5 minutes")
// @Max(value = 180, message = "visit duration must be at most 180 minutes")
// @Column(nullable = false)
// private Integer visitDuration;


 private String imageUrl;


    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Zone zone;

    @OneToOne
    @JoinColumn
    private Location location;//ماعليها جاسون اقنور معناته اتاكد في الكرد مافي تكرار

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pavilion")
    private Set<Review> reviews;//نفس الشي من ناحية

    @ManyToMany(mappedBy = "pavilions")
    @JsonIgnore
    private Set<Roadmap> roadmaps;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pavilion")
    @JsonIgnore
    private Set<Visit> visits;
}
