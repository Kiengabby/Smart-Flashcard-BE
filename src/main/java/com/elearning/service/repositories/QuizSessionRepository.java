package com.elearning.service.repositories;

import com.elearning.service.entities.QuizSession;
import com.elearning.service.entities.User;
import com.elearning.service.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface cho QuizSession entity
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    /**
     * Tìm session quiz đang active của user cho deck cụ thể
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.deck = :deck AND qs.status = 'ACTIVE'")
    Optional<QuizSession> findActiveSessionByUserAndDeck(@Param("user") User user, @Param("deck") Deck deck);

    /**
     * Đếm số quiz session đã hoàn thành của user
     */
    @Query("SELECT COUNT(qs) FROM QuizSession qs WHERE qs.user = :user AND qs.status = 'COMPLETED'")
    Long countCompletedSessionsByUser(@Param("user") User user);

    /**
     * Tìm session quiz gần nhất đã hoàn thành của user cho deck cụ thể
     */
    @Query("SELECT qs FROM QuizSession qs WHERE qs.user = :user AND qs.deck = :deck AND qs.status = 'COMPLETED' ORDER BY qs.completedAt DESC")
    List<QuizSession> findLatestCompletedSessionByUserAndDeck(@Param("user") User user, @Param("deck") Deck deck, Pageable pageable);
}