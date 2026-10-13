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
public class Entrance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "name cannot be empty")
    @Size(max = 50, message = "name should be 50 letters max")
    @Column(columnDefinition = "VARCHAR(50) not null unique")
    private String name;

    @OneToOne
    @JoinColumn
    private Location location;

    @OneToMany(mappedBy = "entrance")
    @JsonIgnore
    private Set<Roadmap> roadmaps;
}
