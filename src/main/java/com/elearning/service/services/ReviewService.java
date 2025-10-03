package com.elearning.service.services;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.dtos.ReviewCardDTO;
import com.elearning.service.dtos.ReviewStatsDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic ôn tập với thuật toán SM-2 (đã được refactor)
 * 
 * @author Smart Flashcard Team
 * @version 2.0.0
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
     * @return Danh sách thẻ cần ôn tập dưới dạng ReviewCardDTO.
     */
    public List<ReviewCardDTO> getReviewCards(Long deckId) {
        // Kiểm tra quyền truy cập deck
        verifyDeckAccess(deckId);
        
        LocalDate today = LocalDate.now();
        log.info("Lấy thẻ cần ôn tập cho deck ID: {} trước ngày: {}", deckId, today);
        
        // Tìm các thẻ cần ôn tập (nextReviewDate <= today)
        List<Card> dueCards = cardRepository.findAllByDeckId(deckId).stream()
                .filter(card -> card.getNextReviewDate() == null || 
                               !card.getNextReviewDate().isAfter(today))
                .collect(Collectors.toList());
        
        log.info("Tìm thấy {} thẻ cần ôn tập cho deck ID: {}", dueCards.size(), deckId);
        
        return dueCards.stream()
                .map(this::mapToReviewCardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Xử lý danh sách câu trả lời của người dùng và cập nhật trạng thái các thẻ theo thuật toán SM-2.
     * 
     * @param deckId ID của bộ thẻ.
     * @param answers Danh sách câu trả lời từ người dùng.
     */
    @Transactional
    public void submitAnswers(Long deckId, List<AnswerDTO> answers) {
        // Kiểm tra quyền truy cập deck
        verifyDeckAccess(deckId);
        
        log.info("Xử lý {} câu trả lời cho deck ID: {}", answers.size(), deckId);
        
        for (AnswerDTO answer : answers) {
            Card card = cardRepository.findById(answer.getCardId())
                    .orElseThrow(() -> new RuntimeException("Card not found with id: " + answer.getCardId()));
            
            // Kiểm tra card thuộc đúng deck
            if (!card.getDeck().getId().equals(deckId)) {
                throw new AccessDeniedException("Card không thuộc deck được chỉ định");
            }
            
            // Áp dụng thuật toán SM-2
            calculateSm2Metrics(card, answer.getQuality());
            
            // Lưu thẻ đã được cập nhật
            cardRepository.save(card);
            
            log.debug("Đã cập nhật card ID: {} với quality: {}, intervalDays mới: {}", 
                    card.getId(), answer.getQuality(), card.getIntervalDays());
        }
        
        log.info("Hoàn thành xử lý {} câu trả lời cho deck ID: {}", answers.size(), deckId);
    }

    /**
     * Tính toán và cập nhật các chỉ số SM-2 cho một thẻ cụ thể.
     * 
     * @param card Thẻ cần cập nhật.
     * @param quality Điểm đánh giá từ người dùng (0-5).
     */
    private void calculateSm2Metrics(Card card, int quality) {
        log.debug("Tính toán SM-2 cho card ID: {}, quality: {}", card.getId(), quality);
        
        // Tính toán easiness factor mới theo công thức SM-2
        double newEasinessFactor = card.getEasinessFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        
        // Đảm bảo easiness factor không nhỏ hơn 1.3
        if (newEasinessFactor < 1.3) {
            newEasinessFactor = 1.3;
        }
        
        card.setEasinessFactor(newEasinessFactor);
        
        // Tính toán interval và repetitions dựa trên quality
        if (quality >= 3) {
            // Câu trả lời đúng
            card.setRepetitions(card.getRepetitions() + 1);
            
            if (card.getRepetitions() == 1) {
                card.setIntervalDays(1);
            } else if (card.getRepetitions() == 2) {
                card.setIntervalDays(6);
            } else {
                // Từ lần thứ 3 trở đi, sử dụng công thức: intervalDays * easinessFactor
                double newInterval = Math.ceil(card.getIntervalDays() * newEasinessFactor);
                card.setIntervalDays((int) newInterval);
            }
        } else {
            // Câu trả lời sai hoặc khó
            card.setRepetitions(0);
            card.setIntervalDays(1); // Đặt lại khoảng thời gian về 1 ngày
        }
        
        // Tính toán ngày ôn tập tiếp theo
        LocalDate nextReviewDate = LocalDate.now().plusDays(card.getIntervalDays());
        card.setNextReviewDate(nextReviewDate);
        
        log.debug("Card ID: {} - EF: {}, Repetitions: {}, IntervalDays: {}, NextReview: {}", 
                card.getId(), card.getEasinessFactor(), card.getRepetitions(), 
                card.getIntervalDays(), nextReviewDate);
    }

    /**
     * Lấy thống kê ôn tập cho một bộ thẻ.
     * 
     * @param deckId ID của bộ thẻ.
     * @return Thống kê ôn tập.
     */
    public ReviewStatsDTO getReviewStats(Long deckId) {
        verifyDeckAccess(deckId);
        
        LocalDate today = LocalDate.now();
        List<Card> allCards = cardRepository.findAllByDeckId(deckId);
        
        // Đếm số thẻ theo các trạng thái khác nhau
        long newCards = allCards.stream()
                .filter(card -> card.getNextReviewDate() == null)
                .count();
        
        long dueCards = allCards.stream()
                .filter(card -> card.getNextReviewDate() != null && 
                               !card.getNextReviewDate().isAfter(today))
                .count();
        
        long totalCards = allCards.size();
        
        return ReviewStatsDTO.builder()
                .newCards((int) newCards)
                .dueCards((int) dueCards)
                .totalCards((int) totalCards)
                .build();
    }

    /**
     * Kiểm tra quyền truy cập đến một bộ thẻ.
     * 
     * @param deckId ID của bộ thẻ.
     * @throws AccessDeniedException nếu người dùng không có quyền truy cập.
     */
    private void verifyDeckAccess(Long deckId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
        
        if (!deck.getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập bộ thẻ này");
        }
    }

    /**
     * Chuyển đổi Card entity thành ReviewCardDTO.
     * 
     * @param card Card entity.
     * @return ReviewCardDTO.
     */
    private ReviewCardDTO mapToReviewCardDTO(Card card) {
        return ReviewCardDTO.builder()
                .id(card.getId())
                .frontText(card.getFront())
                .backText(card.getBack())
                .easinessFactor(card.getEasinessFactor())
                .repetitions(card.getRepetitions())
                .interval(card.getIntervalDays())
                .nextReviewDate(card.getNextReviewDate())
                .build();
    }
}