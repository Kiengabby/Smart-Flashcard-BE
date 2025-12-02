package com.elearning.service.repositories;

import com.elearning.service.entities.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    
    /**
     * Find learning progress for a specific user and deck
     */
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.id = :userId AND lp.deck.id = :deckId")
    Optional<LearningProgress> findByUserIdAndDeckId(
        @Param("userId") Long userId, 
        @Param("deckId") Long deckId
    );
    
    /**
     * Find all learning progress for a user
     */
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.id = :userId ORDER BY lp.updatedAt DESC")
    List<LearningProgress> findAllByUserId(@Param("userId") Long userId);
    
    /**
     * Find all learning progress for a deck
     */
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.deck.id = :deckId")
    List<LearningProgress> findAllByDeckId(@Param("deckId") Long deckId);
    
    /**
     * Find all fully completed progress for a user
     */
    @Query("SELECT lp FROM LearningProgress lp WHERE lp.user.id = :userId AND lp.isFullyCompleted = true ORDER BY lp.completedAt DESC")
    List<LearningProgress> findCompletedByUserId(@Param("userId") Long userId);
    
    /**
     * Count completed decks for a user
     */
    @Query("SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user.id = :userId AND lp.isFullyCompleted = true")
    Long countCompletedDecksByUserId(@Param("userId") Long userId);
    
    /**
     * Get average progress for a user
     */
    @Query("SELECT AVG(lp.overallProgress) FROM LearningProgress lp WHERE lp.user.id = :userId")
    Double getAverageProgressByUserId(@Param("userId") Long userId);
}
