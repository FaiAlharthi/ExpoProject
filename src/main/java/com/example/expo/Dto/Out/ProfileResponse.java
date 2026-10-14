package com.example.expo.Dto.Out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// profile without the password
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Integer id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}
