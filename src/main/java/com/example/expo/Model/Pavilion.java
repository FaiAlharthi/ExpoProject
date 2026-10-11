package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Zone zone;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Location location;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pavilion")
    private Set<Review> reviews;

    @ManyToMany(mappedBy = "pavilions")
    @JsonIgnore
    private Set<Roadmap> roadmaps;

    @ManyToMany(mappedBy = "pavilions")
    @JsonIgnore
    private Set<Passport> passports;
}
