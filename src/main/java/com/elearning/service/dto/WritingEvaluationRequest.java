package com.elearning.service.dto;

import lombok.Data;

@Data
public class WritingEvaluationRequest {
    private String word;
    private String meaning;
    private String sentence; // Changed from userSentence to match FE
    private String language; // "en" for English, "vi" for Vietnamese
}
