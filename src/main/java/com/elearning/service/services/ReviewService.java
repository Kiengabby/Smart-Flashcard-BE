package com.elearning.service.services;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.dtos.ReviewCardDTO;
import com.elearning.service.dtos.ReviewStatsDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.exceptions.CustomException;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Service xử lý logic ôn tập với thuật toán SM-2
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    /**
     * Lấy danh sách các thẻ cần ôn tập trong một bộ thẻ cụ thể.
     * Một thẻ được coi là "cần ôn tập" nếu nextReviewDate của nó là trong quá khứ hoặc hôm nay.
     * 
     * @param deckId ID của bộ thẻ.
     * @return Danh sách thẻ cần ôn tập.
     */
    public List<Card> getDueCards(Long deckId) {
        // Kiểm tra quyền truy cập deck
        verifyDeckAccess(deckId);
        
        Date today = new Date();
        log.info("Lấy thẻ cần ôn tập cho deck ID: {} trước ngày: {}", deckId, today);
        
        List<Card> dueCards = cardRepository.findAllByDeckIdAndNextReviewDateBefore(deckId, today);
        
        // Nếu thẻ chưa có nextReviewDate (thẻ mới), cũng cần ôn tập
        List<Card> newCards = cardRepository.findAllByDeckIdAndNextReviewDateIsNull(deckId);
        dueCards.addAll(newCards);
        
        log.info("Tìm thấy {} thẻ cần ôn tập cho deck ID: {}", dueCards.size(), deckId);
        return dueCards;
    }

    /**
     * Xử lý câu trả lời của người dùng cho một thẻ và cập nhật lịch ôn tập
     * dựa trên thuật toán SM-2.
     * 
     * @param answerDTO chứa ID của thẻ và chất lượng câu trả lời.
     */
    @Transactional
    public void processAnswer(AnswerDTO answerDTO) {
        Card card = cardRepository.findById(answerDTO.getCardId())
                .orElseThrow(() -> new CustomException("Không tìm thấy thẻ với ID: " + answerDTO.getCardId(), HttpStatus.NOT_FOUND));

        // Kiểm tra quyền truy cập
        verifyCardAccess(card);
        
        int quality = answerDTO.getQuality();
        
        log.info("Xử lý câu trả lời cho thẻ ID: {} với chất lượng: {}", card.getId(), quality);
        
        // Hiện thực thuật toán SM-2
        calculateNextReview(card, quality);
        
        cardRepository.save(card);
        
        log.info("Đã cập nhật thẻ ID: {}. Lần ôn tập tiếp theo: {}, Interval: {} ngày", 
                card.getId(), card.getNextReviewDate(), card.getInterval());
    }
    
    /**
     * Tính toán lịch ôn tập tiếp theo dựa trên thuật toán SM-2
     * 
     * @param card Thẻ cần tính toán
     * @param quality Chất lượng câu trả lời (0-5)
     */
    private void calculateNextReview(Card card, int quality) {
        if (quality >= 3) {
            // Trường hợp trả lời ĐÚNG
            if (card.getRepetitions() == 0) {
                card.setInterval(1);
            } else if (card.getRepetitions() == 1) {
                card.setInterval(6);
            } else {
                double newInterval = Math.ceil(card.getInterval() * card.getEasinessFactor());
                card.setInterval((int) newInterval);
            }
            card.setRepetitions(card.getRepetitions() + 1);
        } else {
            // Trường hợp trả lời SAI
            card.setRepetitions(0); // Reset số lần lặp lại
            card.setInterval(1);   // Đặt lại khoảng thời gian về 1 ngày
        }

        // Cập nhật hệ số dễ (Easiness Factor)
        double newEasinessFactor = card.getEasinessFactor() + 
                (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        
        if (newEasinessFactor < 1.3) {
            card.setEasinessFactor(1.3); // EF không được thấp hơn 1.3
        } else {
            card.setEasinessFactor(newEasinessFactor);
        }

        // Tính toán và đặt ngày ôn tập tiếp theo
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, card.getInterval());
        card.setNextReviewDate(calendar.getTime());
    }
    
    /**
     * Kiểm tra quyền truy cập deck
     * 
     * @param deckId ID của deck
     */
    private void verifyDeckAccess(Long deckId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new CustomException("Không tìm thấy bộ thẻ với ID: " + deckId, HttpStatus.NOT_FOUND));
        
        if (!deck.getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập bộ thẻ này");
        }
    }
    
    /**
     * Kiểm tra quyền truy cập thẻ
     * 
     * @param card Thẻ cần kiểm tra
     */
    private void verifyCardAccess(Card card) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!card.getDeck().getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập thẻ này");
        }
    }
    
    /**
     * Lấy thống kê ôn tập của một deck
     * 
     * @param deckId ID của deck
     * @return Thông tin thống kê
     */
    public ReviewStatsDTO getReviewStats(Long deckId) {
        verifyDeckAccess(deckId);
        
        List<Card> allCards = cardRepository.findAllByDeckId(deckId);
        List<Card> dueCards = getDueCards(deckId);
        
        long newCards = allCards.stream()
                .filter(card -> card.getNextReviewDate() == null)
                .count();
                
        long reviewCards = dueCards.size() - newCards;
        
        return ReviewStatsDTO.builder()
                .totalCards(allCards.size())
                .newCards((int) newCards)
                .dueCards(dueCards.size())
                .reviewCards((int) reviewCards)
                .build();
    }
    
    /**
     * Chuyển đổi Card entity thành ReviewCardDTO
     * 
     * @param card Card entity
     * @return ReviewCardDTO
     */
    public ReviewCardDTO convertToReviewCardDTO(Card card) {
        Date today = new Date();
        boolean isNewCard = card.getNextReviewDate() == null;
        boolean isDue = isNewCard || (card.getNextReviewDate() != null && card.getNextReviewDate().before(today));
        
        int daysOverdue = 0;
        if (card.getNextReviewDate() != null && card.getNextReviewDate().before(today)) {
            long diffInMillis = today.getTime() - card.getNextReviewDate().getTime();
            daysOverdue = (int) (diffInMillis / (24 * 60 * 60 * 1000));
        }
        
        return ReviewCardDTO.builder()
                .id(card.getId())
                .frontText(card.getFrontText())
                .backText(card.getBackText())
                .deckId(card.getDeck().getId())
                .deckName(card.getDeck().getName())
                .repetitions(card.getRepetitions())
                .easinessFactor(card.getEasinessFactor())
                .interval(card.getInterval())
                .nextReviewDate(card.getNextReviewDate())
                .isNewCard(isNewCard)
                .isDue(isDue)
                .daysOverdue(daysOverdue)
                .build();
    }
}