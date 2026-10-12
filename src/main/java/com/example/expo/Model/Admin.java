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
    @Id
    private Integer id;

    @NotEmpty(message = "job title cannot be empty")
    @Size(max = 50, message = "job title should be 50 letters max")
    @Column(columnDefinition = "VARCHAR(50) not null")
    private String jobTitle;

    @OneToOne
    @MapsId
    @JsonIgnore
    private Profile profile;
}
