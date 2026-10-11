package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Visitor {
    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JsonIgnore
    private Profile profile;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "visitor")
    private AiChatbot aiChatbot;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "visitor")
    private Passport passport;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "visitor")
    private Set<Roadmap> roadmaps;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "visitor")
    private Set<Review> reviews;

    @ManyToMany
    private Set<Interest> interests;
}
