package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Review {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Rating cannot be empty")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @Size(max = 500, message = "A comment must be less than 500 characters")
    @Column(length = 500)
    private String comment;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    //Relations
    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Pavilion pavilion;

    @ManyToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;
}
