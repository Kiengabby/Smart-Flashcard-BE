package com.elearning.service.repositories;

import com.elearning.service.entities.Card;
import com.elearning.service.entities.User;
import com.elearning.service.entities.UserCardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho UserCardProgress entity
 * Quản lý tiến độ học tập của người dùng trên từng thẻ
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Repository
public interface UserCardProgressRepository extends JpaRepository<UserCardProgress, String> {
    
    /**
     * Tìm bản ghi tiến độ theo user và card
     * 
     * @param user User entity
     * @param card Card entity
     * @return Optional UserCardProgress
     */
    Optional<UserCardProgress> findByUserAndCard(User user, Card card);
    
    /**
     * Tìm tất cả các thẻ đã đến hạn ôn tập cho một người dùng cụ thể
     * Bao gồm cả thẻ mới (nextReviewDate = null) và thẻ đến hạn
     * 
     * @param user User entity
     * @param today Ngày hiện tại
     * @return List các UserCardProgress cần ôn tập
     */
    @Query("SELECT ucp FROM UserCardProgress ucp " +
           "WHERE ucp.user = :user " +
           "AND (ucp.nextReviewDate IS NULL OR ucp.nextReviewDate <= :today) " +
           "ORDER BY ucp.nextReviewDate ASC NULLS FIRST")
    List<UserCardProgress> findDueForReview(@Param("user") User user, @Param("today") LocalDate today);
    
    /**
     * Tìm tất cả tiến độ của một người dùng cho một deck cụ thể
     * 
     * @param user User entity
     * @param deckId ID của deck
     * @return List UserCardProgress
     */
    @Query("SELECT ucp FROM UserCardProgress ucp " +
           "WHERE ucp.user = :user AND ucp.card.deck.id = :deckId")
    List<UserCardProgress> findByUserAndDeckId(@Param("user") User user, @Param("deckId") Long deckId);
    
    /**
     * Đếm số thẻ đã đến hạn ôn tập cho một người dùng
     * 
     * @param user User entity
     * @param today Ngày hiện tại
     * @return Số lượng thẻ cần ôn tập
     */
    @Query("SELECT COUNT(ucp) FROM UserCardProgress ucp " +
           "WHERE ucp.user = :user " +
           "AND (ucp.nextReviewDate IS NULL OR ucp.nextReviewDate <= :today)")
    Long countDueForReview(@Param("user") User user, @Param("today") LocalDate today);
    
    /**
     * Tìm tất cả thẻ mới (chưa học lần nào) của một người dùng trong một deck
     * 
     * @param user User entity
     * @param deckId ID của deck
     * @return List UserCardProgress của thẻ mới
     */
    @Query("SELECT ucp FROM UserCardProgress ucp " +
           "WHERE ucp.user = :user " +
           "AND ucp.card.deck.id = :deckId " +
           "AND ucp.nextReviewDate IS NULL")
    List<UserCardProgress> findNewCardsByUserAndDeck(@Param("user") User user, @Param("deckId") Long deckId);
    
    /**
     * Xóa tất cả tiến độ của một thẻ khi thẻ bị xóa
     * 
     * @param card Card entity
     */
    void deleteByCard(Card card);
    
    /**
     * Xóa tất cả tiến độ của một người dùng khi user bị xóa
     * 
     * @param user User entity
     */
    void deleteByUser(User user);
}