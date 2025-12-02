package com.elearning.service.services;

import com.elearning.service.dtos.LearningProgressDTO;
import com.elearning.service.dtos.UpdateLearningProgressRequest;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.LearningProgress;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.DeckRepository;
import com.elearning.service.repositories.LearningProgressRepository;
import com.elearning.service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LearningProgressService {
    
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    
    /**
     * Get or create learning progress for a deck
     */
    public LearningProgressDTO getDeckProgress(Long userId, Long deckId) {
        log.info("Getting learning progress for user {} and deck {}", userId, deckId);
        
        LearningProgress progress = learningProgressRepository
            .findByUserIdAndDeckId(userId, deckId)
            .orElseGet(() -> createInitialProgress(userId, deckId));
        
        return convertToDTO(progress);
    }
    
    /**
     * Update learning progress when completing a mode
     */
    public LearningProgressDTO updateProgress(Long userId, Long deckId, UpdateLearningProgressRequest request) {
        log.info("Updating progress for user {}, deck {}, mode {}", userId, deckId, request.getMode());
        
        LearningProgress progress = learningProgressRepository
            .findByUserIdAndDeckId(userId, deckId)
            .orElseGet(() -> createInitialProgress(userId, deckId));
        
        // Update based on mode
        Timestamp now = new Timestamp(System.currentTimeMillis());
        switch (request.getMode().toLowerCase()) {
            case "flashcard":
                progress.setFlashcardCompleted(request.getCompleted());
                progress.setFlashcardScore(request.getScore());
                if (request.getCompleted()) {
                    progress.setFlashcardCompletedAt(now);
                }
                break;
            case "quiz":
                progress.setQuizCompleted(request.getCompleted());
                progress.setQuizScore(request.getScore());
                if (request.getCompleted()) {
                    progress.setQuizCompletedAt(now);
                }
                break;
            case "listening":
                progress.setListeningCompleted(request.getCompleted());
                progress.setListeningScore(request.getScore());
                if (request.getCompleted()) {
                    progress.setListeningCompletedAt(now);
                }
                break;
            case "writing":
                progress.setWritingCompleted(request.getCompleted());
                progress.setWritingScore(request.getScore());
                if (request.getCompleted()) {
                    progress.setWritingCompletedAt(now);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid mode: " + request.getMode());
        }
        
        // Recalculate overall progress
        progress.calculateOverallProgress();
        
        LearningProgress saved = learningProgressRepository.save(progress);
        log.info("Progress updated successfully. Overall progress: {}%", saved.getOverallProgress());
        
        return convertToDTO(saved);
    }
    
    /**
     * Get all progress for a user
     */
    public List<LearningProgressDTO> getUserProgress(Long userId) {
        return learningProgressRepository.findAllByUserId(userId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get statistics for a user
     */
    public UserProgressStats getUserStats(Long userId) {
        Long completedDecks = learningProgressRepository.countCompletedDecksByUserId(userId);
        Double avgProgress = learningProgressRepository.getAverageProgressByUserId(userId);
        
        return UserProgressStats.builder()
            .completedDecks(completedDecks)
            .averageProgress(avgProgress != null ? avgProgress : 0.0)
            .build();
    }
    
    /**
     * Reset progress for a deck
     */
    public void resetDeckProgress(Long userId, Long deckId) {
        learningProgressRepository.findByUserIdAndDeckId(userId, deckId)
            .ifPresent(progress -> {
                progress.setFlashcardCompleted(false);
                progress.setQuizCompleted(false);
                progress.setListeningCompleted(false);
                progress.setWritingCompleted(false);
                progress.setFlashcardScore(null);
                progress.setQuizScore(null);
                progress.setListeningScore(null);
                progress.setWritingScore(null);
                progress.calculateOverallProgress();
                learningProgressRepository.save(progress);
            });
    }
    
    // ===== PRIVATE METHODS =====
    
    private LearningProgress createInitialProgress(Long userId, Long deckId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new IllegalArgumentException("Deck not found: " + deckId));
        
        LearningProgress progress = LearningProgress.builder()
            .user(user)
            .deck(deck)
            .flashcardCompleted(false)
            .quizCompleted(false)
            .listeningCompleted(false)
            .writingCompleted(false)
            .overallProgress(0)
            .isFullyCompleted(false)
            .build();
        
        return learningProgressRepository.save(progress);
    }
    
    private LearningProgressDTO convertToDTO(LearningProgress entity) {
        return LearningProgressDTO.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .deckId(entity.getDeck().getId())
            .flashcardCompleted(entity.getFlashcardCompleted())
            .flashcardScore(entity.getFlashcardScore())
            .flashcardCompletedAt(entity.getFlashcardCompletedAt())
            .quizCompleted(entity.getQuizCompleted())
            .quizScore(entity.getQuizScore())
            .quizCompletedAt(entity.getQuizCompletedAt())
            .listeningCompleted(entity.getListeningCompleted())
            .listeningScore(entity.getListeningScore())
            .listeningCompletedAt(entity.getListeningCompletedAt())
            .writingCompleted(entity.getWritingCompleted())
            .writingScore(entity.getWritingScore())
            .writingCompletedAt(entity.getWritingCompletedAt())
            .overallProgress(entity.getOverallProgress())
            .isFullyCompleted(entity.getIsFullyCompleted())
            .completedAt(entity.getCompletedAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserProgressStats {
        private Long completedDecks;
        private Double averageProgress;
    }
}
