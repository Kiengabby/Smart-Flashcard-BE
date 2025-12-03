package com.elearning.service.repositories;

import com.elearning.service.entities.SpacedRepetition;
import com.elearning.service.entities.User;
import com.elearning.service.entities.SpacedRepetition.LearningPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SpacedRepetition entity operations
 * Provides specialized query methods for spaced repetition algorithm
 * 
 * @author ManHKien
 * @version 1.0.0
 * @since 2024-12-19
 */
@Repository
public interface SpacedRepetitionRepository extends JpaRepository<SpacedRepetition, Long> {
    
    /**
     * Find spaced repetition record by user and card
     */
    Optional<SpacedRepetition> findByUser_IdAndCard_Id(Long userId, Long cardId);
    
    /**
     * Find all spaced repetition records for a user
     */
    List<SpacedRepetition> findByUser_Id(Long userId);
    
    /**
     * Get all cards due for review for a specific user
     * Cards are due when nextReviewDate is before or equal to current time
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :currentTime " +
           "ORDER BY sr.nextReviewDate ASC, sr.updatedAt ASC")
    List<SpacedRepetition> findDueCardsForUser(@Param("userId") Long userId, 
                                               @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get cards in specific learning phase for a user
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase = :phase " +
           "ORDER BY sr.updatedAt ASC")
    List<SpacedRepetition> findCardsByLearningPhase(@Param("userId") Long userId, 
                                                    @Param("phase") LearningPhase phase);
    
    /**
     * Get prioritized cards for review (low easiness factor, overdue cards)
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :currentTime " +
           "AND (sr.easinessFactor < 2.0 OR sr.nextReviewDate < :priorityThreshold) " +
           "ORDER BY sr.easinessFactor ASC, sr.nextReviewDate ASC")
    List<SpacedRepetition> findPriorityCardsForUser(@Param("userId") Long userId,
                                                    @Param("currentTime") LocalDateTime currentTime,
                                                    @Param("priorityThreshold") LocalDateTime priorityThreshold);
    
    /**
     * Get new cards for a user (cards that haven't been reviewed yet)
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase = 'NEW' " +
           "AND sr.repetitions = 0 " +
           "ORDER BY sr.createdAt ASC")
    List<SpacedRepetition> findNewCardsForUser(@Param("userId") Long userId);
    
    /**
     * Get cards in learning phase (recently learned, need frequent review)
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase = 'LEARNING' " +
           "AND sr.nextReviewDate <= :currentTime " +
           "ORDER BY sr.nextReviewDate ASC")
    List<SpacedRepetition> findLearningCardsForUser(@Param("userId") Long userId,
                                                    @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get cards ready for graduation to review phase
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase = 'LEARNING' " +
           "AND sr.repetitions >= 2 " +
           "AND sr.easinessFactor >= 2.5")
    List<SpacedRepetition> findCardsReadyForGraduation(@Param("userId") Long userId);
    
    /**
     * Get mastered cards for user
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase = 'MASTERED' " +
           "ORDER BY sr.masteryLevel DESC")
    List<SpacedRepetition> findMasteredCardsForUser(@Param("userId") Long userId);
    
    /**
     * Count cards by learning phase for analytics
     */
    @Query("SELECT sr.learningPhase, COUNT(sr) FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "GROUP BY sr.learningPhase")
    List<Object[]> countCardsByPhase(@Param("userId") Long userId);
    
    /**
     * Get average easiness factor for user (learning difficulty indicator)
     */
    @Query("SELECT AVG(sr.easinessFactor) FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.learningPhase IN ('REVIEW', 'MASTERED')")
    Optional<Double> getAverageEasinessFactorForUser(@Param("userId") Long userId);
    
    /**
     * Get cards with low mastery level that need attention
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.masteryLevel < :threshold " +
           "ORDER BY sr.masteryLevel ASC, sr.updatedAt ASC")
    List<SpacedRepetition> findCardsNeedingAttention(@Param("userId") Long userId,
                                                     @Param("threshold") Double threshold);
    
    /**
     * Find cards due for review in the next specified hours
     */
    @Query("SELECT sr FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate BETWEEN :now AND :futureTime " +
           "ORDER BY sr.nextReviewDate ASC")
    List<SpacedRepetition> findCardsUpcoming(@Param("userId") Long userId,
                                             @Param("now") LocalDateTime now,
                                             @Param("futureTime") LocalDateTime futureTime);
    
    /**
     * Get total number of reviews completed by user
     */
    @Query("SELECT SUM(sr.repetitions) FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId")
    Optional<Long> getTotalReviewsForUser(@Param("userId") Long userId);
    
    /**
     * Delete all spaced repetition records for a user (for account deletion)
     */
    void deleteByUser_Id(Long userId);
    
    /**
     * Delete spaced repetition record for specific card
     */
    void deleteByUser_IdAndCard_Id(Long userId, Long cardId);
    
    /**
     * Check if user has any cards to review today
     */
    @Query("SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END " +
           "FROM SpacedRepetition sr " +
           "WHERE sr.user.id = :userId " +
           "AND sr.nextReviewDate <= :endOfToday")
    boolean hasCardsToReviewToday(@Param("userId") Long userId, 
                                  @Param("endOfToday") LocalDateTime endOfToday);
    
    /**
     * Delete methods for cascade deletion
     */
    void deleteByCardIdIn(List<Long> cardIds);
}
