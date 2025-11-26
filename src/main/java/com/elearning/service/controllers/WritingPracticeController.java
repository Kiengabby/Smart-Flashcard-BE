package com.elearning.service.controllers;

import com.elearning.service.dto.WritingEvaluationRequest;
import com.elearning.service.dto.WritingFeedbackResponse;
import com.elearning.service.services.WritingPracticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/writing-practice")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201", "http://localhost:4202"})
public class WritingPracticeController {

    private final WritingPracticeService writingPracticeService;

    /**
     * Evaluate a user's sentence using AI - Enhanced for Modal Display
     */
    @PostMapping("/evaluate")
    public ResponseEntity<WritingFeedbackResponse> evaluateSentence(
            @RequestBody WritingEvaluationRequest request,
            Authentication authentication) {
        
        try {
            log.info("Evaluating sentence for user: {}, word: {}", 
                    authentication != null ? authentication.getName() : "anonymous", request.getWord());
            
            WritingFeedbackResponse feedback = writingPracticeService.evaluateSentence(request);
            
            // Enhance response for modal display
            feedback = enhanceResponseForModal(feedback, request);
            
            return ResponseEntity.ok(feedback);
            
        } catch (Exception e) {
            log.error("Error evaluating sentence: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createFallbackResponse(request));
        }
    }

    /**
     * Enhance response with additional data for modal display
     */
    private WritingFeedbackResponse enhanceResponseForModal(WritingFeedbackResponse feedback, WritingEvaluationRequest request) {
        // Add original word and sentence for context in modal
        if (feedback.getPositivePoints() == null || feedback.getPositivePoints().isEmpty()) {
            feedback.setPositivePoints(List.of("Bạn đã cố gắng sử dụng từ vựng mới!"));
        }
        
        if (feedback.getImprovementAreas() == null || feedback.getImprovementAreas().isEmpty()) {
            feedback.setImprovementAreas(List.of("Tiếp tục luyện tập để cải thiện!"));
        }
        
        // Ensure all fields are populated for modal
        if (feedback.getGrammarCheck() == null || feedback.getGrammarCheck().isEmpty()) {
            feedback.setGrammarCheck("Ngữ pháp cơ bản đã được áp dụng đúng.");
        }
        
        if (feedback.getVocabularyLevel() == null || feedback.getVocabularyLevel().isEmpty()) {
            feedback.setVocabularyLevel("Intermediate");
        }
        
        return feedback;
    }

    /**
     * Create fallback response for errors
     */
    private WritingFeedbackResponse createFallbackResponse(WritingEvaluationRequest request) {
        return WritingFeedbackResponse.builder()
                .score(7)
                .suggestion("Không thể đánh giá câu lúc này, nhưng bạn đã viết một câu có ý nghĩa!")
                .positivePoints(List.of("Bạn đã cố gắng sử dụng từ mới", "Câu có cấu trúc cơ bản"))
                .improvementAreas(List.of("Thử lại sau để nhận được đánh giá chi tiết"))
                .grammarCheck("Hệ thống tạm thời không khả dụng")
                .vocabularyLevel("Intermediate")
                .isCorrect(true)
                .build();
    }

    /**
     * Get example sentences for a word
     */
    @PostMapping("/example")
    public ResponseEntity<Map<String, String>> getExampleSentence(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String word = request.get("word");
            String meaning = request.get("meaning");
            
            String userName = authentication != null ? authentication.getName() : "anonymous";
            log.info("Getting example sentence for word: {} by user: {}", word, userName);
            
            String example = writingPracticeService.generateExampleSentence(word, meaning);
            
            // Parse JSON response from AI
            Map<String, String> response;
            try {
                // Try to parse as JSON
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> parsed = mapper.readValue(example, Map.class);
                response = Map.of(
                    "exampleSentence", parsed.getOrDefault("example", "I use this word daily."),
                    "explanation", parsed.getOrDefault("translation", "Bản dịch không có sẵn.")
                );
            } catch (Exception e) {
                // Fallback if not JSON
                response = Map.of(
                    "exampleSentence", example,
                    "explanation", "Đây là một ví dụ về cách sử dụng từ '" + word + "' trong câu."
                );
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating example sentence: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "exampleSentence", "I use this word in my daily life.",
                        "explanation", "Có lỗi xảy ra khi tạo ví dụ."
                    ));
        }
    }
}
