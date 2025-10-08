package com.elearning.service.services;

import com.elearning.service.dtos.AnswerDTO;
import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.ReviewCardDTO;
import com.elearning.service.dtos.ReviewStatsDTO;
import com.elearning.service.entities.Card;
import com.elearning.service.entities.Deck;
import com.elearning.service.entities.User;
import com.elearning.service.entities.UserCardProgress;
import com.elearning.service.repositories.CardRepository;
import com.elearning.service.repositories.DeckRepository;
import com.elearning.service.repositories.UserRepository;
import com.elearning.service.repositories.UserCardProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic ôn tập với thuật toán SM-2 (Spaced Repetition System)
 * 
 * @author Smart Flashcard Team
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserCardProgressRepository userCardProgressRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final ModelMapper modelMapper;

    /**
     * Lấy danh sách các thẻ cần ôn tập hôm nay cho người dùng hiện tại
     * 
     * @return List<CardDTO> - Danh sách thẻ cần ôn tập
     */
    @Transactional(readOnly = true)
    public List<CardDTO> getReviewsForToday() {
        log.info("Bắt đầu lấy danh sách thẻ cần ôn tập hôm nay");
        
        // Lấy thông tin người dùng đang đăng nhập
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả các bản ghi UserCardProgress đã đến hạn ôn tập
        List<UserCardProgress> dueProgressList = userCardProgressRepository.findDueForReview(currentUser, today);
        
        log.info("Tìm thấy {} thẻ cần ôn tập cho user: {}", dueProgressList.size(), currentUser.getEmail());
        
        // Chuyển đổi sang CardDTO
        return dueProgressList.stream()
                .map(progress -> {
                    Card card = progress.getCard();
                    CardDTO cardDTO = modelMapper.map(card, CardDTO.class);
                    
                    // ModelMapper sẽ tự động map các field cần thiết
                    return cardDTO;
                })
                .collect(Collectors.toList());
    }

    /**
     * Xử lý câu trả lời của người dùng và cập nhật tiến độ học tập theo thuật toán SM-2
     * 
     * @param answerDTO DTO chứa cardId và quality (0-5)
     */
    @Transactional
    public void submitAnswer(AnswerDTO answerDTO) {
        log.info("Xử lý câu trả lời cho card ID: {}, quality: {}", answerDTO.getCardId(), answerDTO.getQuality());
        
        // Validate input
        if (answerDTO.getQuality() < 0 || answerDTO.getQuality() > 5) {
            throw new IllegalArgumentException("Quality phải trong khoảng từ 0 đến 5");
        }
        
        // Lấy thông tin người dùng và thẻ
        User currentUser = getCurrentUser();
        Card card = cardRepository.findById(answerDTO.getCardId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + answerDTO.getCardId()));
        
        // Kiểm tra quyền truy cập (user có quyền học thẻ này không)
        if (!card.getDeck().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập thẻ này");
        }
        
        // Tìm hoặc tạo bản ghi tiến độ
        UserCardProgress progress = userCardProgressRepository.findByUserAndCard(currentUser, card)
                .orElseGet(() -> createNewProgress(currentUser, card));
        
        // Áp dụng thuật toán SM-2
        applySM2Algorithm(progress, answerDTO.getQuality());
        
        // Lưu bản ghi đã cập nhật
        userCardProgressRepository.save(progress);
        
        log.info("Đã cập nhật tiến độ cho card ID: {}, EF: {}, interval: {}, repetitions: {}, nextReview: {}", 
                card.getId(), progress.getEaseFactor(), progress.getInterval(), 
                progress.getRepetitions(), progress.getNextReviewDate());
    }

    /**
     * Lấy thống kê ôn tập cho người dùng hiện tại
     * 
     * @return ReviewStatsDTO - Thống kê ôn tập
     */
    @Transactional(readOnly = true)
    public ReviewStatsDTO getReviewStats() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        
        Long dueCount = userCardProgressRepository.countDueForReview(currentUser, today);
        
        return ReviewStatsDTO.builder()
                .dueCards(dueCount.intValue())
                .totalCards(0) // Có thể tính toán thêm nếu cần
                .newCards(0)   // Có thể tính toán thêm nếu cần
                .reviewCards(0) // Có thể tính toán thêm nếu cần
                .build();
    }

    /**
     * Tạo bản ghi tiến độ mới cho user và card
     */
    private UserCardProgress createNewProgress(User user, Card card) {
        return UserCardProgress.builder()
                .user(user)
                .card(card)
                .easeFactor(2.5)
                .interval(0)
                .repetitions(0)
                .nextReviewDate(null)
                .lastReviewedDate(null)
                .totalReviews(0)
                .correctReviews(0)
                .build();
    }

    /**
     * Áp dụng thuật toán SM-2 để tính toán các thông số mới
     * 
     * @param progress Bản ghi tiến độ hiện tại
     * @param quality Chất lượng câu trả lời (0-5)
     */
    private void applySM2Algorithm(UserCardProgress progress, int quality) {
        LocalDate today = LocalDate.now();
        
        // Cập nhật thống kê
        progress.setTotalReviews(progress.getTotalReviews() + 1);
        progress.setLastReviewedDate(today);
        
        if (quality >= 3) {
            progress.setCorrectReviews(progress.getCorrectReviews() + 1);
        }
        
        // Tính toán easeFactor mới theo công thức SM-2
        double newEaseFactor = progress.getEaseFactor() + 
                (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        
        // Đảm bảo easeFactor không thấp hơn 1.3
        if (newEaseFactor < 1.3) {
            newEaseFactor = 1.3;
        }
        progress.setEaseFactor(newEaseFactor);
        
        if (quality < 3) {
            // Trả lời sai: Reset về đầu
            log.debug("Trả lời sai (quality < 3), reset tiến độ");
            progress.setRepetitions(0);
            progress.setInterval(1);
        } else {
            // Trả lời đúng: Tăng tiến độ
            log.debug("Trả lời đúng (quality >= 3), tăng tiến độ");
            progress.setRepetitions(progress.getRepetitions() + 1);
            
            // Tính interval mới theo SM-2
            int newInterval;
            if (progress.getRepetitions() == 1) {
                newInterval = 1;
            } else if (progress.getRepetitions() == 2) {
                newInterval = 6;
            } else {
                // repetitions > 2: interval = interval_cũ * easeFactor
                newInterval = (int) Math.ceil(progress.getInterval() * newEaseFactor);
            }
            
            progress.setInterval(newInterval);
        }
        
        // Tính ngày ôn tập tiếp theo
        LocalDate nextReviewDate = today.plusDays(progress.getInterval());
        progress.setNextReviewDate(nextReviewDate);
        
        log.debug("SM-2 kết quả: EF={}, interval={}, repetitions={}, nextReview={}", 
                progress.getEaseFactor(), progress.getInterval(), 
                progress.getRepetitions(), nextReviewDate);
    }

    /**
     * Lấy thông tin người dùng hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
    }

    /**
     * Kiểm tra quyền truy cập đến một bộ thẻ
     */
    private void verifyDeckAccess(Long deckId) {
        User currentUser = getCurrentUser();
        
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
        
        if (!deck.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập bộ thẻ này");
        }
    }
}