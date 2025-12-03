package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for starting a daily review session
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReviewSessionRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Builder.Default
    private Map<String, Object> preferences = new HashMap<>();
    
    // Common preferences
    private Integer maxCardsPerSession = 20;
    private Boolean prioritizeOverdue = true;
    private Boolean includeLearningCards = true;
    private Boolean includeReviewCards = true;
    private String difficulty; // "easy", "medium", "hard", "mixed"
}
