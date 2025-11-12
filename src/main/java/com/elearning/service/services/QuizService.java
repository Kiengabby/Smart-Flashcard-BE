package com.elearning.service.services;

import com.elearning.service.dtos.*;
import com.elearning.service.entities.*;
import com.elearning.service.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý logic quiz recognition
 * 
 * @author Smart Flashcard Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizSessionRepository quizSessionRepository;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final ObjectMapper objectMapper;

    /**
     * Bắt đầu quiz mới cho deck
     */
    @Transactional
    public QuizQuestionDTO startQuiz(Long deckId) {
        log.info("Bắt đầu quiz cho deck ID: {}", deckId);
        
        User currentUser = getCurrentUser();
        Deck deck = getDeckWithAccessCheck(deckId, currentUser);
        
        // Lấy tất cả cards trong deck
        List<Card> cards = cardRepository.findAllByDeckId(deckId);
        if (cards.size() < 4) {
            throw new IllegalStateException("Deck phải có ít nhất 4 thẻ để có thể làm quiz");
        }
        
        // Hủy session cũ nếu có
        Optional<QuizSession> existingSession = quizSessionRepository.findActiveSessionByUserAndDeck(currentUser, deck);
        existingSession.ifPresent(session -> {
            session.setStatus(QuizSession.QuizStatus.CANCELLED);
            quizSessionRepository.save(session);
        });
        
        // Xáo trộn thứ tự cards
        Collections.shuffle(cards);
        
        // Tạo session mới
        List<Long> cardIds = cards.stream().map(Card::getId).collect(Collectors.toList());
        String cardIdsJson = convertToJson(cardIds);
        
        QuizSession session = QuizSession.builder()
                .user(currentUser)
                .deck(deck)
                .status(QuizSession.QuizStatus.ACTIVE)
                .currentQuestion(1)
                .totalQuestions(cards.size())
                .cardIds(cardIdsJson)
                .correctAnswers(0)
                .wrongAnswers(0)
                .correctCardIds("[]")
                .wrongCardIds("[]")
                .build();
        
        session = quizSessionRepository.save(session);
        log.info("Tạo quiz session ID: {} với {} câu hỏi", session.getId(), cards.size());
        
        // Trả về câu hỏi đầu tiên
        return generateQuestion(session, cards.get(0), cards);
    }

    /**
     * Lấy câu hỏi hiện tại của session
     */
    @Transactional(readOnly = true)
    public QuizQuestionDTO getCurrentQuestion(Long deckId) {
        log.info("Lấy câu hỏi hiện tại cho deck ID: {}", deckId);
        
        User currentUser = getCurrentUser();
        Deck deck = getDeckWithAccessCheck(deckId, currentUser);
        
        QuizSession session = quizSessionRepository.findActiveSessionByUserAndDeck(currentUser, deck)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy session quiz active"));
        
        List<Long> cardIds = convertFromJson(session.getCardIds(), new TypeReference<List<Long>>() {});
        Long currentCardId = cardIds.get(session.getCurrentQuestion() - 1);
        
        Card currentCard = cardRepository.findById(currentCardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy card ID: " + currentCardId));
        
        // Kiểm tra xem câu hỏi đã được tạo chưa
        if (session.getCurrentOptions() != null && session.getCurrentCorrectAnswerIndex() != null) {
            // Sử dụng options đã lưu
            List<String> options = convertFromJson(session.getCurrentOptions(), new TypeReference<List<String>>() {});
            return QuizQuestionDTO.builder()
                    .cardId(currentCard.getId())
                    .questionNumber(session.getCurrentQuestion())
                    .totalQuestions(session.getTotalQuestions())
                    .question(currentCard.getFront())
                    .options(options)
                    .correctAnswerIndex(session.getCurrentCorrectAnswerIndex())
                    .build();
        } else {
            // Tạo câu hỏi mới
            List<Card> allCards = cardRepository.findAllByDeckId(deckId);
            return generateQuestion(session, currentCard, allCards);
        }
    }

    /**
     * Submit câu trả lời
     */
    @Transactional
    public QuizAnswerResultDTO submitAnswer(Long deckId, QuizAnswerDTO answerDTO) {
        log.info("Submit câu trả lời cho deck ID: {}, card ID: {}, đáp án: {}", 
                deckId, answerDTO.getCardId(), answerDTO.getSelectedAnswerIndex());
        
        User currentUser = getCurrentUser();
        Deck deck = getDeckWithAccessCheck(deckId, currentUser);
        
        QuizSession session = quizSessionRepository.findActiveSessionByUserAndDeck(currentUser, deck)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy session quiz active"));
        
        // Validate card ID
        List<Long> cardIds = convertFromJson(session.getCardIds(), new TypeReference<List<Long>>() {});
        if (cardIds.isEmpty() || session.getCurrentQuestion() <= 0 || session.getCurrentQuestion() > cardIds.size()) {
            throw new IllegalStateException("Dữ liệu session không hợp lệ");
        }
        
        Long currentCardId = cardIds.get(session.getCurrentQuestion() - 1);
        
        if (!currentCardId.equals(answerDTO.getCardId())) {
            throw new IllegalArgumentException("Card ID không khớp với câu hỏi hiện tại");
        }
        
        // Lấy options và correct index từ session (đã được lưu khi tạo câu hỏi)
        List<String> currentOptions = convertFromJson(session.getCurrentOptions(), new TypeReference<List<String>>() {});
        Integer correctAnswerIndex = session.getCurrentCorrectAnswerIndex();
        
        if (currentOptions.isEmpty() || correctAnswerIndex == null || 
            correctAnswerIndex < 0 || correctAnswerIndex >= currentOptions.size()) {
            throw new IllegalStateException("Dữ liệu câu hỏi không hợp lệ");
        }
        
        if (answerDTO.getSelectedAnswerIndex() == null || 
            answerDTO.getSelectedAnswerIndex() < 0 || 
            answerDTO.getSelectedAnswerIndex() >= currentOptions.size()) {
            throw new IllegalArgumentException("Đáp án được chọn không hợp lệ");
        }
        
        // Kiểm tra đáp án
        boolean isCorrect = answerDTO.getSelectedAnswerIndex().equals(correctAnswerIndex);
        
        // Cập nhật session
        updateSessionWithAnswer(session, currentCardId, isCorrect);
        
        // Tạo kết quả
        QuizAnswerResultDTO result = QuizAnswerResultDTO.builder()
                .isCorrect(isCorrect)
                .correctAnswerIndex(correctAnswerIndex)
                .correctAnswer(currentOptions.get(correctAnswerIndex))
                .selectedAnswer(currentOptions.get(answerDTO.getSelectedAnswerIndex()))
                .build();
        
        // Kiểm tra xem còn câu hỏi nào không
        if (session.getCurrentQuestion() < session.getTotalQuestions()) {
            // Còn câu hỏi -> tạo câu hỏi tiếp theo
            session.setCurrentQuestion(session.getCurrentQuestion() + 1);
            session = quizSessionRepository.save(session);
            
            Long nextCardId = cardIds.get(session.getCurrentQuestion() - 1);
            Card nextCard = cardRepository.findById(nextCardId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy card ID: " + nextCardId));
            
            List<Card> allCards = cardRepository.findAllByDeckId(deckId);
            result.setNextQuestion(generateQuestion(session, nextCard, allCards));
        } else {
            // Hết câu hỏi -> hoàn thành quiz
            session.setStatus(QuizSession.QuizStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            quizSessionRepository.save(session);
            
            result.setNextQuestion(null);
        }
        
        return result;
    }

    /**
     * Lấy kết quả quiz
     */
    @Transactional(readOnly = true)
    public QuizResultDTO getQuizResult(Long deckId) {
        log.info("Lấy kết quả quiz cho deck ID: {}", deckId);
        
        User currentUser = getCurrentUser();
        Deck deck = getDeckWithAccessCheck(deckId, currentUser);
        
        List<QuizSession> sessions = quizSessionRepository.findLatestCompletedSessionByUserAndDeck(
                currentUser, deck, org.springframework.data.domain.PageRequest.of(0, 1));
        
        if (sessions.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy session quiz đã hoàn thành");
        }
        
        QuizSession session = sessions.get(0);
        
        // Tính toán thời gian hoàn thành
        long totalSeconds = ChronoUnit.SECONDS.between(session.getStartedAt(), session.getCompletedAt());
        
        // Tính phần trăm chính xác
        double accuracy = (double) session.getCorrectAnswers() / session.getTotalQuestions() * 100;
        
        // Tạo thông báo khuyến khích
        String message = generateEncouragementMessage(accuracy);
        
        return QuizResultDTO.builder()
                .deckId(deckId)
                .totalQuestions(session.getTotalQuestions())
                .correctAnswers(session.getCorrectAnswers())
                .wrongAnswers(session.getWrongAnswers())
                .accuracyPercentage(Math.round(accuracy * 100.0) / 100.0)
                .totalTimeSeconds(totalSeconds)
                .correctCardIds(convertFromJson(session.getCorrectCardIds(), new TypeReference<List<Long>>() {}))
                .wrongCardIds(convertFromJson(session.getWrongCardIds(), new TypeReference<List<Long>>() {}))
                .message(message)
                .build();
    }

    /**
     * Tạo câu hỏi quiz với 4 lựa chọn
     */
    private QuizQuestionDTO generateQuestion(QuizSession session, Card questionCard, List<Card> allCards) {
        // Lấy đáp án đúng
        String correctAnswer = questionCard.getBack();
        
        // Tạo 3 đáp án sai từ các cards khác
        List<String> wrongAnswers = allCards.stream()
                .filter(card -> !card.getId().equals(questionCard.getId()))
                .map(Card::getBack)
                .filter(back -> !back.equals(correctAnswer)) // Tránh trùng lặp
                .collect(Collectors.toList());
        
        Collections.shuffle(wrongAnswers);
        
        // Lấy 3 đáp án sai đầu tiên
        List<String> selectedWrongAnswers = wrongAnswers.stream()
                .limit(3)
                .collect(Collectors.toList());
        
        // Nếu không đủ 3 đáp án sai, tạo thêm đáp án giả
        while (selectedWrongAnswers.size() < 3) {
            selectedWrongAnswers.add("Đáp án " + (selectedWrongAnswers.size() + 1));
        }
        
        // Tạo danh sách 4 lựa chọn
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.addAll(selectedWrongAnswers);
        
        // Xáo trộn thứ tự
        Collections.shuffle(options);
        
        // Tìm index của đáp án đúng sau khi xáo trộn
        int correctAnswerIndex = options.indexOf(correctAnswer);
        
        // Lưu thông tin câu hỏi hiện tại vào session
        session.setCurrentOptions(convertToJson(options));
        session.setCurrentCorrectAnswerIndex(correctAnswerIndex);
        quizSessionRepository.save(session);
        
        return QuizQuestionDTO.builder()
                .cardId(questionCard.getId())
                .questionNumber(session.getCurrentQuestion())
                .totalQuestions(session.getTotalQuestions())
                .question(questionCard.getFront())
                .options(options)
                .correctAnswerIndex(correctAnswerIndex)
                .build();
    }

    /**
     * Cập nhật session với câu trả lời
     */
    private void updateSessionWithAnswer(QuizSession session, Long cardId, boolean isCorrect) {
        if (isCorrect) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            
            List<Long> correctIds = convertFromJson(session.getCorrectCardIds(), new TypeReference<List<Long>>() {});
            correctIds.add(cardId);
            session.setCorrectCardIds(convertToJson(correctIds));
        } else {
            session.setWrongAnswers(session.getWrongAnswers() + 1);
            
            List<Long> wrongIds = convertFromJson(session.getWrongCardIds(), new TypeReference<List<Long>>() {});
            wrongIds.add(cardId);
            session.setWrongCardIds(convertToJson(wrongIds));
        }
        
        quizSessionRepository.save(session);
    }

    /**
     * Tạo thông báo khuyến khích dựa trên độ chính xác
     */
    private String generateEncouragementMessage(double accuracy) {
        if (accuracy >= 90) {
            return "Xuất sắc! Bạn đã thành thạo rất tốt bộ thẻ này! 🎉";
        } else if (accuracy >= 80) {
            return "Rất tốt! Bạn đang tiến bộ đáng kể! 👏";
        } else if (accuracy >= 70) {
            return "Khá tốt! Hãy tiếp tục luyện tập để cải thiện nhé! 💪";
        } else if (accuracy >= 60) {
            return "Bạn đang trên đường học hỏi. Đừng bỏ cuộc nhé! 📚";
        } else {
            return "Đừng lo lắng! Mọi người đều bắt đầu từ đây. Hãy ôn lại và thử lần nữa! 🌟";
        }
    }

    /**
     * Lấy thông tin user hiện tại
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));
    }

    /**
     * Lấy deck và kiểm tra quyền truy cập
     */
    private Deck getDeckWithAccessCheck(Long deckId, User user) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy deck với ID: " + deckId));
        
        if (!deck.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập deck này");
        }
        
        return deck;
    }

    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi convert object to JSON", e);
            throw new RuntimeException("Lỗi khi xử lý dữ liệu JSON", e);
        }
    }

    /**
     * Convert JSON string to object
     */
    private <T> T convertFromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.trim().isEmpty()) {
            log.warn("JSON string is null or empty, returning empty list");
            return (T) new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi convert JSON to object: {}", json, e);
            return (T) new ArrayList<>(); // Return empty list as fallback
        }
    }
}