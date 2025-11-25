package com.elearning.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for translation result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResultDto {
    private String word;
    private String translation;
    private Double confidence;
}
