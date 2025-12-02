package com.elearning.service.controllers;

import com.elearning.service.dtos.LearningProgressDTO;
import com.elearning.service.dtos.UpdateLearningProgressRequest;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.UserRepository;
import com.elearning.service.services.LearningProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-progress")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LearningProgressController {
    
    private final LearningProgressService learningProgressService;
    private final UserRepository userRepository;
    
    @GetMapping("/deck/{deckId}")
    public ResponseEntity<LearningProgressDTO> getDeckProgress(
            @PathVariable Long deckId,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {
        try {
            // For testing: allow userId from query param if auth is null
            Long effectiveUserId = userId != null ? userId : getUserIdFromAuth(authentication);
            
            LearningProgressDTO progress = learningProgressService.getDeckProgress(effectiveUserId, deckId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("Error getting deck progress for deckId: " + deckId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/deck/{deckId}")
    public ResponseEntity<LearningProgressDTO> updateProgress(
            @PathVariable Long deckId,
            @RequestParam(required = false) Long userId,
            @RequestBody UpdateLearningProgressRequest request,
            Authentication authentication) {
        try {
            // For testing: allow userId from query param if auth is null
            Long effectiveUserId = userId != null ? userId : getUserIdFromAuth(authentication);
            
            LearningProgressDTO progress = learningProgressService.updateProgress(effectiveUserId, deckId, request);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("Error updating progress for deckId: " + deckId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<LearningProgressDTO>> getUserProgress(
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<LearningProgressDTO> progressList = learningProgressService.getUserProgress(userId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            log.error("Error getting user progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<LearningProgressService.UserProgressStats> getUserStats(
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            LearningProgressService.UserProgressStats stats = learningProgressService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/deck/{deckId}")
    public ResponseEntity<Void> resetDeckProgress(
            @PathVariable Long deckId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            learningProgressService.resetDeckProgress(userId, deckId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error resetting progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Helper method to get User ID from Authentication object
     * Authentication.getName() returns email, so we need to query User entity
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            return 1L; // Default for testing
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        return user.getId();
    }
}
