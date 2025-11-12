package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyStatsDTO {
    private long totalCards;
    private long dueCards;
    private long completedToday;
    private long currentStreak;
    private long longestStreak;
    private double averageQuality;
    private long totalDecks;
    private long studyingDecks;
    private long conqueredDecks;
    private long reviewToday;
    private long totalWordsLearned;
    private long activeChallenges;
}
