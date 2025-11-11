package com.elearning.service.services;

import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.CreateCardDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import com.elearning.service.entities.UserCardProgress;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import com.elearning.service.repositories.UserRepository;
import com.elearning.service.repositories.UserCardProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final UserCardProgressRepository userCardProgressRepository;

    public CardDTO createCard(Long deckId, CreateCardDTO createCardDTO) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + deckId));
        
        // Kiểm tra quyền sở hữu
        if (!deck.getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền thêm thẻ vào bộ thẻ này");
        }
        
        Card card = new Card();
        card.setFront(createCardDTO.getFrontText());
        card.setBack(createCardDTO.getBackText());
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
        
        // Lấy thông tin user hiện tại
        User currentUser = getCurrentUser();
        
        List<Card> cards = cardRepository.findAllByDeckId(deckId);
        
        return cards.stream()
                .map(card -> mapToCardDTOWithProgress(card, currentUser))
                .collect(Collectors.toList());
    }

    private CardDTO mapToCardDTO(Card card) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        cardDTO.setFrontText(card.getFront());
        cardDTO.setBackText(card.getBack());
        
        // Các trường spaced repetition sẽ được lấy từ UserCardProgress
        // Tạm thời set giá trị mặc định
        cardDTO.setRepetitions(0);
        cardDTO.setEasinessFactor(2.5);
        cardDTO.setInterval(0);
        cardDTO.setNextReviewDate(null);
        
        return cardDTO;
    }

    /**
     * Map Card entity to CardDTO with actual user progress data
     */
    private CardDTO mapToCardDTOWithProgress(Card card, User user) {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(card.getId());
        cardDTO.setFrontText(card.getFront());
        cardDTO.setBackText(card.getBack());
        
        // Lấy tiến độ học tập từ UserCardProgress
        Optional<UserCardProgress> progressOpt = userCardProgressRepository.findByUserAndCard(user, card);
        
        if (progressOpt.isPresent()) {
            UserCardProgress progress = progressOpt.get();
            cardDTO.setRepetitions(progress.getRepetitions());
            cardDTO.setEasinessFactor(progress.getEaseFactor());
            cardDTO.setInterval(progress.getInterval());
            
            // Chuyển LocalDate sang Date
            if (progress.getNextReviewDate() != null) {
                cardDTO.setNextReviewDate(java.sql.Date.valueOf(progress.getNextReviewDate()));
            } else {
                cardDTO.setNextReviewDate(null);
            }
        } else {
            // Nếu chưa có tiến độ, sử dụng giá trị mặc định
            cardDTO.setRepetitions(0);
            cardDTO.setEasinessFactor(2.5);
            cardDTO.setInterval(0);
            cardDTO.setNextReviewDate(null);
        }
        
        return cardDTO;
    }

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
    }

    private Card getAndVerifyCardOwnership(Long cardId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + cardId));
        
        // Kiểm tra quyền sở hữu thông qua Deck
        if (!card.getDeck().getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập thẻ này");
        }
        
        return card;
    }

    public CardDTO updateCard(Long cardId, CreateCardDTO cardDetails) {
        Card card = getAndVerifyCardOwnership(cardId);
        
        card.setFront(cardDetails.getFrontText());
        card.setBack(cardDetails.getBackText());
        
        Card updatedCard = cardRepository.save(card);
        
        return mapToCardDTO(updatedCard);
    }

    public void deleteCard(Long cardId) {
        getAndVerifyCardOwnership(cardId);
        cardRepository.deleteById(cardId);
    }
}
