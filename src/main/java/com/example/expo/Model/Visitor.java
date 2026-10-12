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
public class Visitor {
    @Id
    private Integer id;

    @NotNull(message = "age cannot be empty")
    @Min(value = 1, message = "age must be at least 1")
    @Max(value = 120, message = "age must be at most 120")
    @Column(columnDefinition = "INTEGER not null")
    private Integer age;

    @NotEmpty(message = "gender cannot be empty")
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "gender must be MALE or FEMALE")
    @Column(columnDefinition = "VARCHAR(6) not null")
    private String gender;

    @NotEmpty(message = "preferred language cannot be empty")
    @Pattern(regexp = "^(AR|EN)$", message = "preferred language must be AR or EN")
    @Column(columnDefinition = "VARCHAR(2) not null")
    private String preferredLanguage;

    @NotEmpty(message = "nationality cannot be empty")
    @Size(max = 50, message = "nationality should be 50 letters max")
    @Column(columnDefinition = "VARCHAR(50) not null")
    private String nationality;

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
