package com.elearning.service.controllers;

import com.elearning.service.dto.CardTranslationData;
import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.CreateCardDTO;
import com.elearning.service.dtos.ReviewCardRequest;
import com.elearning.service.dtos.BulkCreateCardsRequest;
import com.elearning.service.dtos.BulkCreateCardsResponse;
import com.elearning.service.services.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDTO> createCard(
            @PathVariable Long deckId,
            @RequestBody @Valid CreateCardDTO cardDTO) {
        CardDTO createdCard = cardService.createCard(deckId, cardDTO);
        return ResponseEntity.ok(createdCard);
    }

    @GetMapping
    public ResponseEntity<List<CardDTO>> getCardsByDeck(@PathVariable Long deckId) {
        List<CardDTO> cards = cardService.getCardsByDeck(deckId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardDTO> updateCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @RequestBody @Valid CreateCardDTO cardDetails) {
        CardDTO updatedCard = cardService.updateCard(cardId, cardDetails);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/review")
    public ResponseEntity<CardDTO> reviewCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @RequestBody ReviewCardRequest request) {
        CardDTO updatedCard = cardService.reviewCard(cardId, request.getQuality());
        return ResponseEntity.ok(updatedCard);
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<BulkCreateCardsResponse> bulkCreateCards(
            @PathVariable Long deckId,
            @RequestBody @Valid BulkCreateCardsRequest request) {
        BulkCreateCardsResponse response = cardService.createCardsWithTranslation(deckId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-from-translations")
    public ResponseEntity<BulkCreateCardsResponse> createCardsFromTranslations(
            @PathVariable Long deckId,
            @RequestBody List<CardTranslationData> cardsData) {
        BulkCreateCardsResponse response = cardService.createCardsFromTranslations(deckId, cardsData);
        return ResponseEntity.ok(response);
    }
}
