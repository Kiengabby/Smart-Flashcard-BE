package com.elearning.service.controllers;

import com.elearning.service.dtos.CreateDeckDTO;
import com.elearning.service.dtos.DeckDTO;
import com.elearning.service.services.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ResponseEntity<List<DeckDTO>> getAllDecks() {
        List<DeckDTO> decks = deckService.getDecksByCurrentUser();
        return ResponseEntity.ok(decks);
    }
    
    @GetMapping("/")
    public ResponseEntity<List<DeckDTO>> getDecksForUser() {
        List<DeckDTO> decks = deckService.getDecksForCurrentUser();
        return ResponseEntity.ok(decks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeckDTO> getDeckById(@PathVariable Long id) {
        DeckDTO deck = deckService.getDeckById(id);
        return ResponseEntity.ok(deck);
    }

    @PostMapping("/")
    public ResponseEntity<DeckDTO> createDeck(@RequestBody @Valid CreateDeckDTO createDeckDTO) {
        DeckDTO createdDeck = deckService.createDeck(createDeckDTO);
        return ResponseEntity.ok(createdDeck);
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<DeckDTO> updateDeck(
            @PathVariable Long deckId, 
            @RequestBody @Valid CreateDeckDTO deckDetails) {
        DeckDTO updatedDeck = deckService.updateDeck(deckId, deckDetails);
        return ResponseEntity.ok(updatedDeck);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
