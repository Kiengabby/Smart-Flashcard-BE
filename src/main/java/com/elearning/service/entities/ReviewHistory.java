package com.elearning.service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "review_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewHistory extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    @Column(name = "quality", nullable = false)
    private Integer quality;
    
    @Column(name = "time_spent")
    private Integer timeSpent;
    
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
    
    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "learning_phase", length = 20)
    private SpacedRepetition.LearningPhase learningPhase;
    
    @Column(name = "interval_days")
    private Integer intervalDays;
    
    @Column(name = "easiness_factor")
    private Double easinessFactor;
    
    @Column(name = "is_successful")
    private Boolean isSuccessful;
    
    @PrePersist
    protected void setDefaults() {
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
        if (reviewDate == null) {
            reviewDate = LocalDate.now();
        }
        if (isSuccessful == null) {
            isSuccessful = quality != null && quality >= 3;
        }
    }
}
