package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    
    public AuthResponseDTO(String token, Long id, String email, String fullName) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }
}
