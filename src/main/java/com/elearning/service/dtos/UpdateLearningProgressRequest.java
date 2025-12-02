package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLearningProgressRequest {
    
    private String mode; // flashcard, quiz, listening, writing
    private Boolean completed;
    private Integer score;
}
