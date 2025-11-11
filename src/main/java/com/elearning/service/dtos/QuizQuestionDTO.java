package com.elearning.service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho một câu hỏi quiz trắc nghiệm
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDTO {
    
    /**
     * ID của thẻ (card) được sử dụng làm câu hỏi
     */
    private Long cardId;
    
    /**
     * Số thứ tự câu hỏi (1, 2, 3, ...)
     */
    private Integer questionNumber;
    
    /**
     * Tổng số câu hỏi trong quiz
     */
    private Integer totalQuestions;
    
    /**
     * Mặt trước của thẻ (câu hỏi)
     */
    private String question;
    
    /**
     * Danh sách 4 lựa chọn đáp án (1 đúng + 3 sai)
     * Thứ tự đã được xáo trộn ngẫu nhiên
     */
    private List<String> options;
    
    /**
     * Index của đáp án đúng trong danh sách options (0-3)
     */
    private Integer correctAnswerIndex;
}