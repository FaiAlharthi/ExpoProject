package com.example.expo.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AiChatbot {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String lastQuestion;

    @Column(columnDefinition = "TEXT")
    private String lastAnswer;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt; // set in service

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime lastInteractionAt; // updated with every question

    //Relations
    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;
}
