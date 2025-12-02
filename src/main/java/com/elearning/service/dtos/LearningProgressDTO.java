package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressDTO {
    
    private Long id;
    private Long userId;
    private Long deckId;
    
    // Flashcard mode
    private Boolean flashcardCompleted;
    private Integer flashcardScore;
    private Timestamp flashcardCompletedAt;
    
    // Quiz mode
    private Boolean quizCompleted;
    private Integer quizScore;
    private Timestamp quizCompletedAt;
    
    // Listening mode
    private Boolean listeningCompleted;
    private Integer listeningScore;
    private Timestamp listeningCompletedAt;
    
    // Writing mode
    private Boolean writingCompleted;
    private Integer writingScore;
    private Timestamp writingCompletedAt;
    
    // Overall progress
    private Integer overallProgress;
    private Boolean isFullyCompleted;
    private Timestamp completedAt;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
