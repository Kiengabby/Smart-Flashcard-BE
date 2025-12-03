package com.elearning.service.controllers;

import com.elearning.service.services.DailyReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Daily Review Controller - Real Implementation
 * Provides RESTful APIs for daily review functionality
 * 
 * @author ManHKien - Smart Flashcard Platform
 * @version 1.0.0
 * @since 2024-12-03
 */
@Slf4j
@RestController
@RequestMapping("/api/daily-review")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201", "http://127.0.0.1:4200", "http://127.0.0.1:4201"})
public class DailyReviewController {

    private final DailyReviewService dailyReviewService;
    
    /**
     * Get daily review overview for user
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getDailyReviewOverview(@RequestParam(defaultValue = "1") Long userId) {
        log.info("Getting daily review overview for user: {}", userId);
        
        try {
            Map<String, Object> overview = dailyReviewService.getDailyReviewOverview(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Daily review overview retrieved successfully",
                "data", overview
            ));
        } catch (Exception e) {
            log.error("Error getting daily review overview for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get daily review overview: " + e.getMessage(),
                "data", null
            ));
        }
    }
    
    /**
     * Start a new daily review session
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<?> startDailyReviewSession(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.getOrDefault("userId", 1).toString());
            Map<String, Object> preferences = (Map<String, Object>) request.getOrDefault("preferences", new HashMap<>());
            
            log.info("Starting daily review session for user: {}", userId);
            
            Map<String, Object> sessionResult = dailyReviewService.startDailyReviewSession(userId, preferences);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Daily review session started successfully",
                "data", sessionResult
            ));
        } catch (Exception e) {
            log.error("Error starting daily review session: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to start daily review session: " + e.getMessage(),
                "data", null
            ));
        }
    }
    
    /**
     * Update card review progress
     */
    @PostMapping("/cards/{cardId}/review")
    public ResponseEntity<?> updateCardReview(
            @PathVariable Long cardId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.getOrDefault("userId", 1).toString());
            Integer quality = (Integer) request.get("quality");
            Integer timeSpent = (Integer) request.getOrDefault("timeSpent", 30);
            
            if (quality == null || quality < 1 || quality > 5) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Quality must be between 1 and 5",
                    "data", null
                ));
            }
            
            log.debug("Updating card review: user={}, card={}, quality={}", userId, cardId, quality);
            
            Map<String, Object> result = dailyReviewService.updateCardReview(userId, cardId, quality, timeSpent);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Card review updated successfully",
                "data", result
            ));
        } catch (Exception e) {
            log.error("Error updating card review for card {}: {}", cardId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to update card review: " + e.getMessage(),
                "data", null
            ));
        }
    }
    
    /**
     * Get learning statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getLearningStatistics(@RequestParam(defaultValue = "1") Long userId) {
        try {
            log.debug("Getting learning statistics for user: {}", userId);
            
            Map<String, Object> statistics = dailyReviewService.getLearningStatistics(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Learning statistics retrieved successfully",
                "data", statistics
            ));
        } catch (Exception e) {
            log.error("Error getting learning statistics for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get learning statistics: " + e.getMessage(),
                "data", null
            ));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Daily Review service is running",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
