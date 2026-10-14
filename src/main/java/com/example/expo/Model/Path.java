package com.example.expo.Model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Path {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Double distance;  // Automatically counted in path service from X to Y

    //Relations
    @ManyToOne
    @JoinColumn
    private Location fromLocation;

    @ManyToOne
    @JoinColumn
    private Location toLocation;
}