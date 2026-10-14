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
public class Passport {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(13) UNIQUE")
    private String passportNumber; // Generated in service, e.g. EXPO30-000012

    @Pattern(regexp = "^(BRONZE|SILVER|GOLD)$", message = "Explorer level must be BRONZE, SILVER or GOLD")
    @Column(columnDefinition = "VARCHAR(6)")
    private String explorerLevel; // Updated by visits count

    //Relations
    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "passport")
    private Set <Visit> visits;
}
