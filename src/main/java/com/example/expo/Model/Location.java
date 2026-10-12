package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "x should not be empty")
    @Column(nullable = false)
    private Double x;

    @NotNull(message = "y should not be empty")
    @Column(nullable = false)
    private Double y;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "location")
    private Zone zone;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "location")
    private Pavilion pavilion;
}
