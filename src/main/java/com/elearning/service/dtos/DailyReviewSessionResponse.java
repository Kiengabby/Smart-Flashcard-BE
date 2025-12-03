package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for daily review session response
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReviewSessionResponse {
    
    private String sessionId;
    private Long userId;
    private LocalDateTime startTime;
    private SessionInfo sessionInfo;
    private List<ReviewCard> cards;
    private SessionSettings settings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private Integer totalCards;
        private Integer remainingCards;
        private Integer completedCards;
        private Double progressPercentage;
        private String estimatedTimeRemaining;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewCard {
        private Long cardId;
        private String front;
        private String back;
        private String audioUrl;
        private String difficulty;
        private String learningPhase;
        private Boolean isOverdue;
        private Integer currentInterval;
        private LocalDateTime nextReviewDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSettings {
        private Integer maxCardsPerSession;
        private Boolean prioritizeOverdue;
        private String difficulty;
        private Boolean shuffleCards;
    }
}
