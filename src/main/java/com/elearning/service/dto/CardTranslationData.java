package com.elearning.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for card translation data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTranslationData {
    private String frontText;
    private String backText;
}
