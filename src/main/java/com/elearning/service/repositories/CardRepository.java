package com.elearning.service.repositories;

import com.elearning.service.entities.Card;
import com.elearning.service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    List<Card> findAllByDeckId(Long deckId);
    
    @Query("SELECT COUNT(c) FROM Card c WHERE c.deck.user = :user")
    long countByDeck_User(@Param("user") User user);
    
    // Delete methods for cascade deletion
    void deleteByDeckId(Long deckId);
}
