package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho kết quả submit câu trả lời quiz
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerResultDTO {
    
    /**
     * Câu trả lời có đúng không
     */
    private Boolean isCorrect;
    
    /**
     * Index của đáp án đúng (0-3)
     */
    private Integer correctAnswerIndex;
    
    /**
     * Đáp án đúng (text)
     */
    private String correctAnswer;
    
    /**
     * Đáp án được chọn (text)
     */
    private String selectedAnswer;
    
    /**
     * Câu hỏi tiếp theo (null nếu đã hết)
     */
    private QuizQuestionDTO nextQuestion;
}