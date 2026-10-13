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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String lastQuestion;

    @Column(columnDefinition = "TEXT")
    private String lastAnswer;

    // set in service
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    // updated with every question
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime lastInteractionAt;

    @OneToOne
    @JoinColumn
    @JsonIgnore
    private Visitor visitor;
}
