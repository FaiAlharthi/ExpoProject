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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "full name cannot be empty")
    @Size(max = 50, message = "full name should be 50 letters max")
    @Column(columnDefinition = "VARCHAR(50) not null")
    private String fullName;

    @NotEmpty(message = "username cannot be empty")
    @Size(min = 4, max = 20, message = "username must be between 4 and 20 characters")
    @Column(columnDefinition = "VARCHAR(20) not null unique")
    private String username;

    //مبدئياً يتخزن بدون تشفير لين نضيف السكيورتي
    //WRITE_ONLY عشان ينرسل في البودي بس ما يطلع في الجيسون
    @NotEmpty(message = "password cannot be empty")
    @Size(min = 8, message = "password must be at least 8 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(columnDefinition = "VARCHAR(255) not null")
    private String password;

    @NotEmpty(message = "email cannot be empty")
    @Email(message = "email must be in a valid form")
    @Column(columnDefinition = "VARCHAR(100) not null unique")
    private String email;

    //رقم دولي برمز الدولة
    @NotEmpty(message = "phone cannot be empty")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "phone must be in international format, e.g. +966501234567")
    @Column(columnDefinition = "VARCHAR(16) not null")
    private String phone;

    //يتعبى تلقائي لما ينضاف Admin او Visitor
    @Pattern(regexp = "^(ADMIN|VISITOR)$", message = "role must be ADMIN or VISITOR")
    @Column(columnDefinition = "VARCHAR(7)")
    private String role;

    //يتعبى تلقائي في السيرفس
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Admin admin;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "profile")
    @PrimaryKeyJoinColumn
    private Visitor visitor;
}
