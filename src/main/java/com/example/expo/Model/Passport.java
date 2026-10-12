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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //يتولد في السيرفس بصيغة EXPO30-000012 (الرقم هو id الزائر)
    @Column(columnDefinition = "VARCHAR(13) unique")
    private String passportNumber;

    //يتحدث تلقائي حسب عدد الزيارات: BRONZE من 0 ، SILVER من 10 ، GOLD من 25
    @Pattern(regexp = "^(BRONZE|SILVER|GOLD)$", message = "explorer level must be BRONZE, SILVER or GOLD")
    @Column(columnDefinition = "VARCHAR(6)")
    private String explorerLevel;

    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "passport")
    private Set <Visit> visits;
}
