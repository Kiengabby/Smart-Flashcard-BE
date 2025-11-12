package com.elearning.service.repositories;

import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    
    List<Deck> findAllByUserId(Long userId);
    
    long countByUser(User user);
    
    @Query("SELECT COUNT(DISTINCT d) FROM Deck d WHERE d.user = :user AND EXISTS (SELECT 1 FROM Card c JOIN UserCardProgress ucp ON c.id = ucp.card.id WHERE c.deck = d AND ucp.user = :user AND ucp.repetitions > 0)")
    long countStudyingDecksByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(DISTINCT d) FROM Deck d WHERE d.user = :user AND NOT EXISTS (SELECT 1 FROM Card c WHERE c.deck = d AND NOT EXISTS (SELECT 1 FROM UserCardProgress ucp WHERE ucp.card = c AND ucp.user = :user AND ucp.easeFactor >= 2.5 AND ucp.interval >= 30))")
    long countConqueredDecksByUser(@Param("user") User user);
}
