package com.elearning.service.services;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho ReviewService và thuật toán SM-2
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private CardRepository cardRepository;
    
    @Mock
    private DeckRepository deckRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private ReviewService reviewService;
    
    private Card testCard;
    private Deck testDeck;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Khởi tạo test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        
        testDeck = new Deck();
        testDeck.setId(1L);
        testDeck.setName("Test Deck");
        testDeck.setUser(testUser);
        
        testCard = new Card();
        testCard.setId(1L);
        testCard.setFrontText("What is Java?");
        testCard.setBackText("A programming language");
        testCard.setDeck(testDeck);
        testCard.setRepetitions(0);
        testCard.setEasinessFactor(2.5);
        testCard.setInterval(0);
        testCard.setNextReviewDate(null);
        
        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
    }
    
    @Test
    void testProcessAnswer_FirstTimeCorrect() {
        // Given
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setCardId(1L);
        answerDTO.setQuality(4); // Correct answer
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        
        // When
        reviewService.processAnswer(answerDTO);
        
        // Then
        assertEquals(1, testCard.getRepetitions());
        assertEquals(1, testCard.getInterval());
        assertNotNull(testCard.getNextReviewDate());
        assertTrue(testCard.getEasinessFactor() >= 1.3);
        
        // Verify next review date is 1 day from now
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        
        Calendar nextReview = Calendar.getInstance();
        nextReview.setTime(testCard.getNextReviewDate());
        
        assertEquals(tomorrow.get(Calendar.DAY_OF_YEAR), 
                    nextReview.get(Calendar.DAY_OF_YEAR));
    }
    
    @Test
    void testProcessAnswer_SecondTimeCorrect() {
        // Given - Card has been answered correctly once
        testCard.setRepetitions(1);
        testCard.setInterval(1);
        
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setCardId(1L);
        answerDTO.setQuality(5); // Perfect answer
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        
        // When
        reviewService.processAnswer(answerDTO);
        
        // Then
        assertEquals(2, testCard.getRepetitions());
        assertEquals(6, testCard.getInterval());
        assertTrue(testCard.getEasinessFactor() > 2.5); // Should increase
        
        // Verify next review date is 6 days from now
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.DAY_OF_YEAR, 6);
        
        Calendar actual = Calendar.getInstance();
        actual.setTime(testCard.getNextReviewDate());
        
        assertEquals(expected.get(Calendar.DAY_OF_YEAR), 
                    actual.get(Calendar.DAY_OF_YEAR));
    }
    
    @Test
    void testProcessAnswer_ThirdTimeCorrect() {
        // Given - Card has been answered correctly twice
        testCard.setRepetitions(2);
        testCard.setInterval(6);
        testCard.setEasinessFactor(2.6);
        
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setCardId(1L);
        answerDTO.setQuality(4);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        
        // When
        reviewService.processAnswer(answerDTO);
        
        // Then
        assertEquals(3, testCard.getRepetitions());
        int expectedInterval = (int) Math.ceil(6 * 2.6); // 16 days
        assertEquals(expectedInterval, testCard.getInterval());
    }
    
    @Test
    void testProcessAnswer_IncorrectAnswer() {
        // Given - Card has been answered correctly before
        testCard.setRepetitions(5);
        testCard.setInterval(30);
        testCard.setEasinessFactor(2.8);
        
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setCardId(1L);
        answerDTO.setQuality(2); // Incorrect answer
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        
        // When
        reviewService.processAnswer(answerDTO);
        
        // Then
        assertEquals(0, testCard.getRepetitions()); // Reset
        assertEquals(1, testCard.getInterval()); // Reset to 1 day
        assertTrue(testCard.getEasinessFactor() < 2.8); // Should decrease
        assertTrue(testCard.getEasinessFactor() >= 1.3); // But not below 1.3
    }
    
    @Test
    void testEasinessFactorCalculation() {
        // Test various quality scores and their effect on EF
        testCard.setEasinessFactor(2.5);
        
        // Quality 5 (perfect) should increase EF
        AnswerDTO perfectAnswer = new AnswerDTO();
        perfectAnswer.setCardId(1L);
        perfectAnswer.setQuality(5);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        
        reviewService.processAnswer(perfectAnswer);
        double efAfterPerfect = testCard.getEasinessFactor();
        assertTrue(efAfterPerfect > 2.5);
        
        // Quality 0 (complete failure) should decrease EF significantly
        testCard.setEasinessFactor(2.5); // Reset
        AnswerDTO failAnswer = new AnswerDTO();
        failAnswer.setCardId(1L);
        failAnswer.setQuality(0);
        
        reviewService.processAnswer(failAnswer);
        double efAfterFail = testCard.getEasinessFactor();
        assertTrue(efAfterFail < 2.5);
        assertTrue(efAfterFail >= 1.3); // Should not go below minimum
    }
    
    @Test
    void testGetDueCards() {
        // Given
        when(deckRepository.findById(1L)).thenReturn(Optional.of(testDeck));
        
        // When
        reviewService.getDueCards(1L);
        
        // Then
        verify(cardRepository).findAllByDeckIdAndNextReviewDateBefore(eq(1L), any(Date.class));
        verify(cardRepository).findAllByDeckIdAndNextReviewDateIsNull(1L);
    }
}