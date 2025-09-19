package com.elearning.service.controllers;

import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.CreateCardDTO;
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
}
