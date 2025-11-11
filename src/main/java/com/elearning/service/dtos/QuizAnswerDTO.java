package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho việc submit câu trả lời quiz
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerDTO {
    
    /**
     * ID của thẻ được trả lời
     */
    private Long cardId;
    
    /**
     * Index của đáp án được chọn (0-3)
     */
    private Integer selectedAnswerIndex;
    
    /**
     * Thời gian trả lời (milliseconds) - có thể dùng sau này
     */
    private Long responseTime;
}