package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Admin {
    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JsonIgnore
    private Profile profile;
}
