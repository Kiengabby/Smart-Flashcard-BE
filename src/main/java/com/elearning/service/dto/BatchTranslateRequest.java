package com.elearning.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for batch translation request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTranslateRequest {
    private List<String> words;
    private String sourceLanguage;
    private String targetLanguage;
    private String context;
    private boolean autoDetectLanguage;
}
