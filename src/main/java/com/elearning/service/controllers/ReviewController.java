package com.elearning.service.controllers;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.ReviewStatsDTO;
import com.elearning.service.dtos.base.ResponseDTO;
import com.elearning.service.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller xử lý các API liên quan đến ôn tập thẻ với thuật toán SM-2
 * 
 * @author Smart Flashcard Team
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Lấy danh sách thẻ cần ôn tập hôm nay cho người dùng hiện tại
     * 
     * @return ResponseEntity chứa danh sách CardDTO cần ôn tập
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<List<CardDTO>>> getTodayReviews() {
        log.info("API: Lấy danh sách thẻ cần ôn tập hôm nay");
        
        try {
            List<CardDTO> reviewCards = reviewService.getReviewsForToday();
            
            ResponseDTO<List<CardDTO>> response = ResponseDTO.success(
                "Lấy thẻ cần ôn tập thành công", 
                reviewCards
            );
            
            log.info("Trả về {} thẻ cần ôn tập", reviewCards.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thẻ ôn tập: {}", e.getMessage(), e);
            
            ResponseDTO<List<CardDTO>> errorResponse = ResponseDTO.error(
                "Lỗi khi lấy thẻ cần ôn tập: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Gửi câu trả lời cho một thẻ và cập nhật tiến độ học tập
     * 
     * @param answerDTO DTO chứa cardId và quality (0-5)
     * @return ResponseEntity xác nhận đã xử lý thành công
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<String>> submitAnswer(
            @Valid @RequestBody AnswerDTO answerDTO) {
        
        log.info("API: Xử lý câu trả lời cho card ID: {}, quality: {}", 
                answerDTO.getCardId(), answerDTO.getQuality());
        
        try {
            reviewService.submitAnswer(answerDTO);
            
            ResponseDTO<String> response = ResponseDTO.success(
                "Câu trả lời đã được xử lý thành công",
                "Tiến độ học tập đã được cập nhật theo thuật toán SM-2"
            );
            
            log.info("Đã xử lý thành công câu trả lời cho card ID: {}", answerDTO.getCardId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Dữ liệu đầu vào không hợp lệ: {}", e.getMessage());
            
            ResponseDTO<String> errorResponse = ResponseDTO.error(
                "Dữ liệu không hợp lệ: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý câu trả lời: {}", e.getMessage(), e);
            
            ResponseDTO<String> errorResponse = ResponseDTO.error(
                "Lỗi hệ thống khi xử lý câu trả lời: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Lấy thống kê ôn tập tổng quan cho người dùng hiện tại
     * 
     * @return ResponseEntity chứa ReviewStatsDTO với thống kê ôn tập
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDTO<ReviewStatsDTO>> getReviewStats() {
        log.info("API: Lấy thống kê ôn tập cho user hiện tại");
        
        try {
            ReviewStatsDTO stats = reviewService.getReviewStats();
            
            ResponseDTO<ReviewStatsDTO> response = ResponseDTO.success(
                "Lấy thống kê ôn tập thành công",
                stats
            );
            
            log.info("Trả về thống kê: {} thẻ cần ôn tập", stats.getDueCards());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê ôn tập: {}", e.getMessage(), e);
            
            ResponseDTO<ReviewStatsDTO> errorResponse = ResponseDTO.error(
                "Lỗi khi lấy thống kê ôn tập: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}