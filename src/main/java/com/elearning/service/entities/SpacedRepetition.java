package com.elearning.service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Spaced Repetition Entity - SM-2 Algorithm Implementation
 * Tracks learning progress for each user-card pair using spaced repetition
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Entity
@Table(name = "spaced_repetition", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}),
       indexes = {
           @Index(name = "idx_user_next_review", columnList = "user_id, next_review_date"),
           @Index(name = "idx_learning_phase", columnList = "user_id, learning_phase"),
           @Index(name = "idx_mastery_level", columnList = "user_id, mastery_level")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetition {

    // Enums
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    public enum LearningPhase {
        NEW, LEARNING, REVIEW, MASTERED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    // SM-2 Algorithm Core Fields
    @Column(name = "easiness_factor", nullable = false)
    @Builder.Default
    private BigDecimal easinessFactor = BigDecimal.valueOf(2.5);

    @Column(name = "repetitions", nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private Integer intervalDays = 1;

    @Column(name = "next_review_date", nullable = false)
    private LocalDateTime nextReviewDate;

    // Learning Progress Tracking
    @Column(name = "quality_responses", columnDefinition = "TEXT")
    private String qualityResponses; // JSON: [5,4,3,5,4]

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "successful_reviews", nullable = false)
    @Builder.Default
    private Integer successfulReviews = 0;

    // Performance Metrics
    @Column(name = "average_response_time")
    private BigDecimal averageResponseTime;

    @Column(name = "accuracy_rate")
    private BigDecimal accuracyRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    // Learning Context
    @Column(name = "last_quality")
    private Integer lastQuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_phase", length = 20)
    @Builder.Default
    private LearningPhase learningPhase = LearningPhase.NEW;

    @Column(name = "mastery_level")
    @Builder.Default
    private BigDecimal masteryLevel = BigDecimal.ZERO;

    // Adaptive Learning Features
    @Column(name = "streak_count")
    @Builder.Default
    private Integer streakCount = 0;

    @Column(name = "last_streak_date")
    private LocalDateTime lastStreakDate;

    @Column(name = "is_priority")
    @Builder.Default
    private Boolean isPriority = false;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business Logic Methods

    /**
     * Calculate accuracy rate based on successful/total reviews
     */
    public BigDecimal calculateAccuracyRate() {
        if (totalReviews == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((double) successfulReviews / totalReviews * 100)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Check if card is due for review
     */
    public boolean isDueForReview() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check if card is overdue
     */
    public boolean isOverdue() {
        if (nextReviewDate == null) return false;
        return nextReviewDate.isBefore(LocalDateTime.now().minusHours(24));
    }

    /**
     * Get days overdue (0 if not overdue)
     */
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(
            nextReviewDate.toLocalDate(), 
            LocalDateTime.now().toLocalDate()
        );
    }

    /**
     * Update learning phase based on current progress
     */
    public void updateLearningPhase() {
        BigDecimal accuracy = calculateAccuracyRate();
        
        if (repetitions == 0 || accuracy.compareTo(BigDecimal.valueOf(50)) < 0) {
            this.learningPhase = LearningPhase.NEW;
        } else if (repetitions < 3 || accuracy.compareTo(BigDecimal.valueOf(70)) < 0) {
            this.learningPhase = LearningPhase.LEARNING;
        } else if (intervalDays >= 21 && accuracy.compareTo(BigDecimal.valueOf(80)) >= 0) {
            this.learningPhase = LearningPhase.MASTERED;
        } else {
            this.learningPhase = LearningPhase.REVIEW;
        }
    }

    /**
     * Calculate mastery level (0-1) based on multiple factors
     */
    public void calculateMasteryLevel() {
        if (totalReviews == 0) {
            this.masteryLevel = BigDecimal.ZERO;
            return;
        }

        // Accuracy factor (0-1)
        BigDecimal accuracyFactor = calculateAccuracyRate().divide(BigDecimal.valueOf(100));
        
        // Stability factor based on interval (0-1)
        BigDecimal stabilityFactor = BigDecimal.valueOf(Math.min(1.0, intervalDays / 30.0));
        
        // Consistency factor based on repetitions (0-1)
        BigDecimal consistencyFactor = BigDecimal.valueOf(Math.min(1.0, repetitions / 10.0));
        
        // Weighted combination
        this.masteryLevel = accuracyFactor.multiply(BigDecimal.valueOf(0.5))
                .add(stabilityFactor.multiply(BigDecimal.valueOf(0.3)))
                .add(consistencyFactor.multiply(BigDecimal.valueOf(0.2)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Check if this card should be prioritized in today's review
     */
    public boolean shouldBePrioritized() {
        return isPriority || isOverdue() || 
               (learningPhase == LearningPhase.LEARNING && streakCount == 0);
    }
}
