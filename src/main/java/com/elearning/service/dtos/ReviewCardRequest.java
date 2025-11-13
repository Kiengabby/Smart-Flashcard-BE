package com.elearning.service.dtos;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request review flashcard
 */
@Data
public class ReviewCardRequest {
    
    @NotNull(message = "Quality is required")
    @Min(value = 1, message = "Quality must be between 1 and 5")
    @Max(value = 5, message = "Quality must be between 1 and 5")
    private Integer quality;
}