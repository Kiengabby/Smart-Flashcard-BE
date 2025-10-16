package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private String token;
    private String refreshToken;
    private UserInfo user;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String displayName;
        private String avatar;
    }
    
    // Constructor for convenience (without refreshToken and avatar)
    public AuthResponseDTO(String token, Long id, String email, String displayName, String message) {
        this.token = token;
        this.user = new UserInfo(id.toString(), email, displayName, null);
        this.message = message;
    }
}
