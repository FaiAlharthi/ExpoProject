package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Admin {

    //Variables
    @Id
    private Integer id;

    @NotEmpty(message = "Job title cannot be empty!")
    @Size(max = 50, message = "Job title should not be longer than 50 letters.")
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String jobTitle;

    //Relations
    @OneToOne
    @MapsId
    @JsonIgnore
    private Profile profile;
}
