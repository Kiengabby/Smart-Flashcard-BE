package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for daily review overview response
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReviewOverviewResponse {
    
    private Long userId;
    private ReviewCounts reviewCounts;
    private LearningMetrics learningMetrics;
    private List<UpcomingCard> upcomingCards;
    private Map<String, Object> streakInfo;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewCounts {
        private Integer dueCards;
        private Integer overdueCards;
        private Integer newCards;
        private Integer totalCards;
        private Integer completedToday;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningMetrics {
        private Double accuracyRate;
        private Double averageTime;
        private Integer totalReviews;
        private Integer successfulReviews;
        private String masteryDistribution;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingCard {
        private Long cardId;
        private String front;
        private String back;
        private String difficulty;
        private String learningPhase;
        private Boolean isOverdue;
        private Integer daysOverdue;
    }
}
