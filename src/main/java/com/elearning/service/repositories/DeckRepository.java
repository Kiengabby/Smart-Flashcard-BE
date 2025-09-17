package com.elearning.service.repositories;

import com.elearning.service.entities.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    
    List<Deck> findAllByUserId(Long userId);
}
