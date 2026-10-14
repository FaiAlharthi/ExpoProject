package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Profile {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Full name cannot be empty")
    @Size(max = 50, message = "Full name should be 50 letters max")
    @Column(columnDefinition = "VARCHAR(50) not null")
    private String fullName;

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Column(columnDefinition = "VARCHAR(20) NOT NULL UNIQUE")
    private String username;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String password; // NOT encrypted for now !!!!!!!!!!!

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email must be in a valid form")
    @Column(columnDefinition = "VARCHAR(100) NOT NULL UNIQUE")
    private String email;

    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone must be in international format, e.g. +966501234567")
    @Column(columnDefinition = "VARCHAR(16) NOT NULL")
    private String phone; // International format

    @Pattern(regexp = "^(ADMIN|VISITOR)$", message = "Role must be ADMIN or VISITOR")
    @Column(columnDefinition = "VARCHAR(7)")
    private String role; // Set when an admin or visitor is added

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt; // Set in service

    //Relations
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Admin admin;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Visitor visitor;
}
