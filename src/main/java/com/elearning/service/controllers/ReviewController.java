package com.elearning.service.controllers;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.ReviewStatsDTO;
import com.elearning.service.dtos.base.ResponseDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các API liên quan đến ôn tập thẻ
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ModelMapper modelMapper;

    /**
     * Lấy danh sách thẻ cần ôn tập trong một deck
     * 
     * @param deckId ID của deck
     * @return Danh sách thẻ cần ôn tập
     */
    @GetMapping("/due")
    public ResponseEntity<ResponseDTO<List<CardDTO>>> getDueCards(@RequestParam Long deckId) {
        log.info("Yêu cầu lấy thẻ cần ôn tập cho deck ID: {}", deckId);
        
        List<Card> dueCards = reviewService.getDueCards(deckId);
        List<CardDTO> dueCardDTOs = dueCards.stream()
                .map(card -> modelMapper.map(card, CardDTO.class))
                .collect(Collectors.toList());
                
        ResponseDTO<List<CardDTO>> response = ResponseDTO.success("Lấy thẻ cần ôn tập thành công", dueCardDTOs);
        
        log.info("Trả về {} thẻ cần ôn tập cho deck ID: {}", dueCardDTOs.size(), deckId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Xử lý câu trả lời của người dùng cho một thẻ
     * 
     * @param answerDTO Thông tin câu trả lời
     * @return Kết quả xử lý
     */
    @PostMapping("/answer")
    public ResponseEntity<ResponseDTO<String>> answerCard(@RequestBody @Valid AnswerDTO answerDTO) {
        log.info("Xử lý câu trả lời cho thẻ ID: {} với chất lượng: {}", 
                answerDTO.getCardId(), answerDTO.getQuality());
        
        reviewService.processAnswer(answerDTO);
        
        ResponseDTO<String> response = ResponseDTO.success("Xử lý câu trả lời thành công", "Thẻ đã được cập nhật lịch ôn tập");
        
        log.info("Đã xử lý thành công câu trả lời cho thẻ ID: {}", answerDTO.getCardId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy thống kê ôn tập của một deck
     * 
     * @param deckId ID của deck
     * @return Thông tin thống kê ôn tập
     */
    @GetMapping("/stats")
    public ResponseEntity<ResponseDTO<ReviewStatsDTO>> getReviewStats(@RequestParam Long deckId) {
        log.info("Yêu cầu lấy thống kê ôn tập cho deck ID: {}", deckId);
        
        ReviewStatsDTO stats = reviewService.getReviewStats(deckId);
        
        ResponseDTO<ReviewStatsDTO> response = ResponseDTO.success("Lấy thống kê ôn tập thành công", stats);
        
        log.info("Thống kê deck ID {}: {} thẻ tổng, {} thẻ mới, {} thẻ cần ôn", 
                deckId, stats.getTotalCards(), stats.getNewCards(), stats.getDueCards());
        
        return ResponseEntity.ok(response);
    }
}