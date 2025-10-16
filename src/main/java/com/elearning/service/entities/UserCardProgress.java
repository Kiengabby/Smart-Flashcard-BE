package com.elearning.service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho tiến độ học tập của người dùng với từng thẻ
 * Sử dụng thuật toán SM-2 (Spaced Repetition System)
 * 
 * @author Smart Flashcard Team
 * @version 2.0.0
 */
@Entity
@Table(name = "user_card_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "card_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /**
     * Hệ số độ dễ (Ease Factor) - mặc định là 2.5
     * Giá trị tối thiểu là 1.3
     */
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private Double easeFactor = 2.5;

    /**
     * Khoảng thời gian (tính bằng ngày) đến lần ôn tập tiếp theo
     */
    @Column(name = "review_interval", nullable = false)
    @Builder.Default
    private Integer interval = 0;

    /**
     * Số lần ôn tập liên tiếp thành công
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer repetitions = 0;

    /**
     * Ngày ôn tập tiếp theo
     */
    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    /**
     * Ngày ôn tập gần nhất
     */
    @Column(name = "last_reviewed_date")
    private LocalDate lastReviewedDate;

    /**
     * Tổng số lần ôn tập (kể cả sai)
     */
    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;

    /**
     * Số lần ôn tập đúng
     */
    @Column(name = "correct_reviews", nullable = false)
    @Builder.Default
    private Integer correctReviews = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Tính toán độ chính xác (%)
     */
    public double getAccuracy() {
        if (totalReviews == 0) {
            return 0.0;
        }
        return (double) correctReviews / totalReviews * 100.0;
    }
}
