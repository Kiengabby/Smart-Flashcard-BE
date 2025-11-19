package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateCardsResponse {
    
    private List<CardDTO> createdCards;
    private List<FailedCardCreation> failedCards;
    private int totalRequested;
    private int successCount;
    private int failureCount;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedCardCreation {
        private String word;
        private String error;
        private String translation; // If translation succeeded but card creation failed
    }
}
