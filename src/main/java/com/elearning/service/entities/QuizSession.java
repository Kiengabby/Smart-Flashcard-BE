package com.elearning.service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity lưu trữ session quiz của user
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Entity
@Table(name = "quiz_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User đang làm quiz
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Deck được quiz
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    /**
     * Trạng thái quiz: ACTIVE, COMPLETED, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuizStatus status;

    /**
     * Câu hỏi hiện tại (1-based index)
     */
    @Column(name = "current_question", nullable = false)
    private Integer currentQuestion;

    /**
     * Tổng số câu hỏi
     */
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    /**
     * Danh sách ID các thẻ theo thứ tự quiz (JSON array)
     */
    @Column(name = "card_ids", columnDefinition = "TEXT")
    private String cardIds;

    /**
     * Options của câu hỏi hiện tại (JSON array)
     */
    @Column(name = "current_options", columnDefinition = "TEXT")
    private String currentOptions;

    /**
     * Index đáp án đúng của câu hỏi hiện tại
     */
    @Column(name = "current_correct_answer_index")
    private Integer currentCorrectAnswerIndex;

    /**
     * Số câu trả lời đúng
     */
    @Column(name = "correct_answers")
    @Builder.Default
    private Integer correctAnswers = 0;

    /**
     * Số câu trả lời sai
     */
    @Column(name = "wrong_answers")
    @Builder.Default
    private Integer wrongAnswers = 0;

    /**
     * Danh sách ID thẻ trả lời đúng (JSON array)
     */
    @Column(name = "correct_card_ids", columnDefinition = "TEXT")
    private String correctCardIds;

    /**
     * Danh sách ID thẻ trả lời sai (JSON array)
     */
    @Column(name = "wrong_card_ids", columnDefinition = "TEXT")
    private String wrongCardIds;

    /**
     * Thời gian bắt đầu quiz
     */
    @CreationTimestamp
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /**
     * Thời gian hoàn thành quiz
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum cho trạng thái quiz
     */
    public enum QuizStatus {
        ACTIVE,     // Đang làm quiz
        COMPLETED,  // Đã hoàn thành
        CANCELLED   // Đã hủy bỏ
    }
}