package com.elearning.service.repositories;

import com.elearning.service.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    List<Card> findAllByDeckId(Long deckId);
    
    /**
     * Tìm tất cả thẻ cần ôn tập (nextReviewDate trước hoặc bằng ngày hiện tại)
     */
    List<Card> findAllByDeckIdAndNextReviewDateBefore(Long deckId, Date today);
    
    /**
     * Tìm tất cả thẻ mới (chưa có nextReviewDate)
     */
    List<Card> findAllByDeckIdAndNextReviewDateIsNull(Long deckId);
}
