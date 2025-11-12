package com.elearning.service.services;

import com.elearning.service.dtos.CardDTO;
import com.elearning.service.dtos.CreateCardDTO;
import com.elearning.service.dtos.StudyStatsDTO;
import com.elearning.service.dtos.StudyStatsDTO;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    /**
     * Lấy thống kê học tập tổng quan của user hiện tại
     */
    public StudyStatsDTO getStudyStats() {
        User currentUser = getCurrentUser();
        LocalDate today = LocalDate.now();
        
        System.out.println("Getting study stats for user: " + currentUser.getEmail()); // Debug log
        
        // Đếm tổng số deck của user
        long totalDecks = deckRepository.countByUser(currentUser);
        System.out.println("Total decks: " + totalDecks); // Debug log
        
        // Đếm tổng số thẻ của user
        long totalCards = cardRepository.countByDeck_User(currentUser);
        System.out.println("Total cards: " + totalCards); // Debug log
        
        // Đếm số thẻ cần ôn tập hôm nay
        long dueCards = 0; // Tạm thời set 0 vì method phức tạp
        
        // Đếm số thẻ đã học hôm nay  
        long completedToday = 0; // Tạm thời set 0 vì method phức tạp
        
        // Tính streak hiện tại (cần implement logic phức tạp hơn)
        long currentStreak = calculateCurrentStreak(currentUser);
        
        // Tính streak dài nhất
        long longestStreak = calculateLongestStreak(currentUser);
        
        // Tính điểm trung bình
        double averageQuality = calculateAverageQuality(currentUser);
        
        // Đếm deck đang học (có ít nhất 1 thẻ đã bắt đầu ôn tập)
        long studyingDecks = 0; // Tạm thời set 0 vì query phức tạp
        
        // Đếm deck đã hoàn thành (logic đơn giản hóa)
        long conqueredDecks = 0; // Tạm thời set 0 vì query phức tạp
        
        return StudyStatsDTO.builder()
                .totalCards(totalCards)
                .dueCards(dueCards)
                .completedToday(completedToday)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .averageQuality(averageQuality)
                .totalDecks(totalDecks)
                .studyingDecks(studyingDecks)
                .conqueredDecks(conqueredDecks)
                .reviewToday(completedToday)
                .totalWordsLearned(totalCards) // Giả sử mỗi thẻ = 1 từ
                .activeChallenges(0) // Tính năng challenges chưa implement
                .build();
    }
    
    private long calculateCurrentStreak(User user) {
        // Lấy tất cả ngày có hoạt động học tập, sắp xếp theo ngày giảm dần
        List<LocalDate> activityDates = userCardProgressRepository
                .findDistinctActivityDatesByUser(user)
                .stream()
                .sorted(Collections.reverseOrder()) // Sắp xếp giảm dần (mới nhất trước)
                .collect(Collectors.toList());
        
        System.out.println("Activity dates for streak calculation: " + activityDates);
        
        if (activityDates.isEmpty()) {
            System.out.println("No activity dates found, streak = 0");
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        System.out.println("Today: " + today + ", Yesterday: " + yesterday);
        
        // Kiểm tra xem có hoạt động hôm nay hoặc hôm qua không
        LocalDate latestActivity = activityDates.get(0);
        System.out.println("Latest activity: " + latestActivity);
        
        if (!latestActivity.equals(today) && !latestActivity.equals(yesterday)) {
            System.out.println("Latest activity not today or yesterday, streak = 0");
            return 0; // Streak đã bị gián đoạn
        }
        
        // Tính streak bằng cách đếm các ngày liên tiếp
        long streak = 0;
        LocalDate expectedDate = latestActivity;
        
        for (LocalDate activityDate : activityDates) {
            System.out.println("Checking date: " + activityDate + ", expected: " + expectedDate);
            if (activityDate.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
                System.out.println("Streak increased to: " + streak + ", next expected: " + expectedDate);
            } else {
                System.out.println("Streak broken at date: " + activityDate);
                break; // Gián đoạn streak
            }
        }
        
        System.out.println("Final calculated streak: " + streak);
        return streak;
    }
    
    private long calculateLongestStreak(User user) {
        // Lấy tất cả ngày có hoạt động học tập, sắp xếp theo ngày tăng dần
        List<LocalDate> activityDates = userCardProgressRepository
                .findDistinctActivityDatesByUser(user)
                .stream()
                .sorted() // Sắp xếp tăng dần
                .collect(Collectors.toList());
        
        if (activityDates.isEmpty()) {
            return 0;
        }
        
        long longestStreak = 1;
        long currentStreak = 1;
        
        for (int i = 1; i < activityDates.size(); i++) {
            LocalDate previousDate = activityDates.get(i - 1);
            LocalDate currentDate = activityDates.get(i);
            
            // Kiểm tra xem ngày hiện tại có liền kề với ngày trước không
            if (currentDate.equals(previousDate.plusDays(1))) {
                currentStreak++;
            } else {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 1;
            }
        }
        
        return Math.max(longestStreak, currentStreak);
    }
    
    private double calculateAverageQuality(User user) {
        // Lấy ease factor trung bình từ UserCardProgress
        Optional<Double> averageEaseFactor = userCardProgressRepository
                .findAverageEaseFactorByUser(user);
        
        if (averageEaseFactor.isPresent()) {
            // Chuyển đổi ease factor thành quality score (từ 2.5 thành 4.0 scale)
            // EF thường từ 1.3-2.5, chuyển thành 0-5 scale
            double easeFactor = averageEaseFactor.get();
            return Math.max(0, Math.min(5, (easeFactor - 1.3) * (5.0 / 1.2)));
        }
        
        return 0.0; // Chưa có dữ liệu
    }
    
    /**
     * Lấy các ngày có hoạt động học tập trong tháng cho user hiện tại
     */
    public java.util.List<Integer> getActivityDatesInMonth(int year, int month) {
        User currentUser = getCurrentUser();
        
        // Tạo LocalDate cho đầu và cuối tháng
        java.time.LocalDate startOfMonth = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        
        // Query để lấy các ngày có activity từ UserCardProgress
        java.util.List<java.time.LocalDate> activityDates = userCardProgressRepository
                .findDistinctActivityDatesByUserInMonth(currentUser, startOfMonth, endOfMonth);
        
        // Chuyển đổi thành list các ngày trong tháng
        return activityDates.stream()
                .map(java.time.LocalDate::getDayOfMonth)
                .collect(java.util.stream.Collectors.toList());
    }
}
