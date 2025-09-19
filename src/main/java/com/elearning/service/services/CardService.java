package com.elearning.service.services;

import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.CreateCardDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    public CardDTO createCard(Long deckId, CreateCardDTO createCardDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + deckId));
        
        // Kiểm tra quyền sở hữu
        if (!deck.getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền thêm thẻ vào bộ thẻ này");
        }
        
        Card card = new Card();
        card.setFrontText(createCardDTO.getFrontText());
        card.setBackText(createCardDTO.getBackText());
        card.setDeck(deck);
        
        Card savedCard = cardRepository.save(card);
        
        return mapToCardDTO(savedCard);
    }

    public List<CardDTO> getCardsByDeck(Long deckId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + deckId));
        
        // Kiểm tra quyền sở hữu
        if (!deck.getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền xem các thẻ trong bộ thẻ này");
        }
        
        List<Card> cards = cardRepository.findAllByDeckId(deckId);
        
        return cards.stream()
                .map(this::mapToCardDTO)
                .collect(Collectors.toList());
    }

    private CardDTO mapToCardDTO(Card card) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        cardDTO.setFrontText(card.getFrontText());
        cardDTO.setBackText(card.getBackText());
        return cardDTO;
    }
}
