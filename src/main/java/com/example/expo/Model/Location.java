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
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "location")
    private Zone zone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    @JsonIgnore
    private Set<Pavilion> pavilions;
}
