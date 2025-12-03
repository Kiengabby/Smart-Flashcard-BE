package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for card review requests in daily review system
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReviewCardRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Quality rating is required")
    @Min(value = 1, message = "Quality must be at least 1")
    @Max(value = 5, message = "Quality must be at most 5")
    private Integer quality;
    
    @Min(value = 1, message = "Time spent must be positive")
    private Integer timeSpent = 30; // default 30 seconds
    
    private String feedback; // optional user feedback
}
