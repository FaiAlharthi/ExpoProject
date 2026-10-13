package com.example.expo.Model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Path {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // تنحسب تلقائي في السيرفس من x و y
    @Column(nullable = false)
    private Double distance;

    @ManyToOne
    @JoinColumn
    private Location fromLocation;

    @ManyToOne
    @JoinColumn
    private Location toLocation;
}