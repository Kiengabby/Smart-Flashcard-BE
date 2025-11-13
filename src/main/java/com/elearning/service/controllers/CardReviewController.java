package com.elearning.service.controllers;

import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.ReviewCardRequest;
import com.elearning.service.services.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardReviewController {

    private final CardService cardService;

    @PostMapping("/{cardId}/review")
    public ResponseEntity<CardDTO> reviewCard(
            @PathVariable Long cardId,
            @RequestBody @Valid ReviewCardRequest request) {
        CardDTO updatedCard = cardService.reviewCard(cardId, request.getQuality());
        return ResponseEntity.ok(updatedCard);
    }
}