package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho kết quả hoàn thành quiz
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDTO {
    
    /**
     * ID của deck được quiz
     */
    private Long deckId;
    
    /**
     * Tổng số câu hỏi
     */
    private Integer totalQuestions;
    
    /**
     * Số câu trả lời đúng
     */
    private Integer correctAnswers;
    
    /**
     * Số câu trả lời sai
     */
    private Integer wrongAnswers;
    
    /**
     * Phần trăm chính xác (0-100)
     */
    private Double accuracyPercentage;
    
    /**
     * Thời gian hoàn thành quiz (seconds)
     */
    private Long totalTimeSeconds;
    
    /**
     * Danh sách ID các thẻ trả lời đúng
     */
    private List<Long> correctCardIds;
    
    /**
     * Danh sách ID các thẻ trả lời sai
     */
    private List<Long> wrongCardIds;
    
    /**
     * Thông báo khuyến khích
     */
    private String message;
}