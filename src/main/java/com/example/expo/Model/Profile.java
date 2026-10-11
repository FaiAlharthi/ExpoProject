package com.example.expo.Model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Admin admin;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Visitor visitor;
}
