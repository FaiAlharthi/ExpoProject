package com.example.expo.Dto.Out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// admin with profile info
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {
    private Integer id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String jobTitle;
}
