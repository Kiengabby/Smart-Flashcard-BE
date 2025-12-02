package com.elearning.service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

/**
 * Entity tracking overall learning progress for a deck
 * Includes completion status for all 4 learning modes: Flashcard, Quiz, Listening, Writing
 */
@Entity
@Table(
    name = "learning_progress",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "deck_id"})
    },
    indexes = {
        @Index(name = "idx_user_deck", columnList = "user_id, deck_id"),
        @Index(name = "idx_deck_id", columnList = "deck_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgress extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;
    
    // ===== FLASHCARD MODE =====
    @Column(name = "flashcard_completed", nullable = false)
    private Boolean flashcardCompleted = false;
    
    @Column(name = "flashcard_score")
    private Integer flashcardScore;
    
    @Column(name = "flashcard_completed_at")
    private Timestamp flashcardCompletedAt;
    
    // ===== QUIZ MODE =====
    @Column(name = "quiz_completed", nullable = false)
    private Boolean quizCompleted = false;
    
    @Column(name = "quiz_score")
    private Integer quizScore;
    
    @Column(name = "quiz_completed_at")
    private Timestamp quizCompletedAt;
    
    // ===== LISTENING MODE =====
    @Column(name = "listening_completed", nullable = false)
    private Boolean listeningCompleted = false;
    
    @Column(name = "listening_score")
    private Integer listeningScore;
    
    @Column(name = "listening_completed_at")
    private Timestamp listeningCompletedAt;
    
    // ===== WRITING MODE =====
    @Column(name = "writing_completed", nullable = false)
    private Boolean writingCompleted = false;
    
    @Column(name = "writing_score")
    private Integer writingScore;
    
    @Column(name = "writing_completed_at")
    private Timestamp writingCompletedAt;
    
    // ===== OVERALL PROGRESS =====
    @Column(name = "overall_progress", nullable = false)
    private Integer overallProgress = 0; // 0-100 percentage
    
    @Column(name = "is_fully_completed", nullable = false)
    private Boolean isFullyCompleted = false;
    
    @Column(name = "completed_at")
    private Timestamp completedAt;
    
    /**
     * Calculate overall progress percentage based on completed modes
     */
    public void calculateOverallProgress() {
        int completed = 0;
        if (Boolean.TRUE.equals(flashcardCompleted)) completed += 25;
        if (Boolean.TRUE.equals(quizCompleted)) completed += 25;
        if (Boolean.TRUE.equals(listeningCompleted)) completed += 25;
        if (Boolean.TRUE.equals(writingCompleted)) completed += 25;
        
        this.overallProgress = completed;
        this.isFullyCompleted = (completed == 100);
        
        if (this.isFullyCompleted && this.completedAt == null) {
            this.completedAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
