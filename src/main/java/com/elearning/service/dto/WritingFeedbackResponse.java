package com.elearning.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WritingFeedbackResponse {
    private Integer score; // Score out of 10
    private String suggestion;
    private List<String> positivePoints;
    private List<String> improvementAreas;
    private String grammarCheck;
    private String vocabularyLevel; // "Beginner", "Intermediate", "Advanced"
    private Boolean isCorrect; // Whether the sentence is correct or not
}
