package com.elearning.service.repositories;

import com.elearning.service.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    List<Card> findAllByDeckId(Long deckId);
}
