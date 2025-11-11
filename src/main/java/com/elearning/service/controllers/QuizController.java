package com.elearning.service.controllers;

import com.elearning.service.dtos.*;
import com.elearning.service.dtos.base.ResponseDTO;
import com.elearning.service.services.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller xử lý các API liên quan đến Quiz Recognition
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/decks/{deckId}/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * Bắt đầu quiz mới cho deck
     * 
     * @param deckId ID của deck cần quiz
     * @return ResponseEntity chứa câu hỏi đầu tiên
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<QuizQuestionDTO>> startQuiz(@PathVariable Long deckId) {
        log.info("API: Bắt đầu quiz cho deck ID: {}", deckId);
        
        try {
            QuizQuestionDTO firstQuestion = quizService.startQuiz(deckId);
            
            ResponseDTO<QuizQuestionDTO> response = ResponseDTO.success(
                "Bắt đầu quiz thành công", 
                firstQuestion
            );
            
            log.info("Tạo quiz thành công với câu hỏi đầu tiên cho card ID: {}", firstQuestion.getCardId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("Không thể bắt đầu quiz: {}", e.getMessage());
            
            ResponseDTO<QuizQuestionDTO> errorResponse = ResponseDTO.error(
                "Không thể bắt đầu quiz: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi bắt đầu quiz: {}", e.getMessage(), e);
            
            ResponseDTO<QuizQuestionDTO> errorResponse = ResponseDTO.error(
                "Lỗi hệ thống khi bắt đầu quiz: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy câu hỏi hiện tại của session quiz
     * 
     * @param deckId ID của deck
     * @return ResponseEntity chứa câu hỏi hiện tại
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<QuizQuestionDTO>> getCurrentQuestion(@PathVariable Long deckId) {
        log.info("API: Lấy câu hỏi hiện tại cho deck ID: {}", deckId);
        
        try {
            QuizQuestionDTO currentQuestion = quizService.getCurrentQuestion(deckId);
            
            ResponseDTO<QuizQuestionDTO> response = ResponseDTO.success(
                "Lấy câu hỏi hiện tại thành công", 
                currentQuestion
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("Không tìm thấy session quiz: {}", e.getMessage());
            
            ResponseDTO<QuizQuestionDTO> errorResponse = ResponseDTO.error(
                "Không tìm thấy session quiz active: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy câu hỏi hiện tại: {}", e.getMessage(), e);
            
            ResponseDTO<QuizQuestionDTO> errorResponse = ResponseDTO.error(
                "Lỗi hệ thống: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Submit câu trả lời cho câu hỏi hiện tại
     * 
     * @param deckId ID của deck
     * @param answerDTO DTO chứa câu trả lời
     * @return ResponseEntity chứa kết quả và câu hỏi tiếp theo (nếu có)
     */
    @PostMapping("/answer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<QuizAnswerResultDTO>> submitAnswer(
            @PathVariable Long deckId,
            @Valid @RequestBody QuizAnswerDTO answerDTO) {
        
        log.info("API: Submit câu trả lời cho deck ID: {}, card ID: {}, đáp án: {}", 
                deckId, answerDTO.getCardId(), answerDTO.getSelectedAnswerIndex());
        
        try {
            QuizAnswerResultDTO result = quizService.submitAnswer(deckId, answerDTO);
            
            String message = result.getIsCorrect() ? 
                "Câu trả lời đúng!" : 
                "Câu trả lời sai. Đáp án đúng là: " + result.getCorrectAnswer();
            
            ResponseDTO<QuizAnswerResultDTO> response = ResponseDTO.success(message, result);
            
            log.info("Xử lý câu trả lời thành công. Kết quả: {}", result.getIsCorrect() ? "ĐÚNG" : "SAI");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Dữ liệu không hợp lệ: {}", e.getMessage());
            
            ResponseDTO<QuizAnswerResultDTO> errorResponse = ResponseDTO.error(
                "Dữ liệu không hợp lệ: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý câu trả lời: {}", e.getMessage(), e);
            
            ResponseDTO<QuizAnswerResultDTO> errorResponse = ResponseDTO.error(
                "Lỗi hệ thống khi xử lý câu trả lời: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy kết quả quiz đã hoàn thành
     * 
     * @param deckId ID của deck
     * @return ResponseEntity chứa kết quả quiz
     */
    @GetMapping("/result")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<QuizResultDTO>> getQuizResult(@PathVariable Long deckId) {
        log.info("API: Lấy kết quả quiz cho deck ID: {}", deckId);
        
        try {
            QuizResultDTO result = quizService.getQuizResult(deckId);
            
            ResponseDTO<QuizResultDTO> response = ResponseDTO.success(
                "Lấy kết quả quiz thành công", 
                result
            );
            
            log.info("Trả về kết quả quiz: {}/{} câu đúng", result.getCorrectAnswers(), result.getTotalQuestions());
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.warn("Không tìm thấy kết quả quiz: {}", e.getMessage());
            
            ResponseDTO<QuizResultDTO> errorResponse = ResponseDTO.error(
                "Không tìm thấy kết quả quiz: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy kết quả quiz: {}", e.getMessage(), e);
            
            ResponseDTO<QuizResultDTO> errorResponse = ResponseDTO.error(
                "Lỗi hệ thống: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}