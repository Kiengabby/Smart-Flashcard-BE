USE smart_flashcard;

-- Tạo bảng quiz_sessions
CREATE TABLE IF NOT EXISTS quiz_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    current_question INT NOT NULL DEFAULT 1,
    total_questions INT NOT NULL,
    card_ids TEXT,
    current_options TEXT,
    current_correct_answer_index INT,
    correct_answers INT DEFAULT 0,
    wrong_answers INT DEFAULT 0,
    correct_card_ids TEXT,
    wrong_card_ids TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    INDEX idx_user_deck (user_id, deck_id),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm dữ liệu demo users
INSERT IGNORE INTO users (email, password, display_name) VALUES 
('kiengabby@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Kiên - Smart Flashcard'),
('demo@smartflashcard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Demo User');

-- Thêm bộ thẻ demo
INSERT IGNORE INTO decks (id, name, description, user_id) VALUES 
(1, 'English Vocabulary', 'Từ vựng tiếng Anh cơ bản cho người mới bắt đầu', 1),
(2, 'TOEIC Essential Words', 'Từ vựng TOEIC cần thiết cho kỳ thi', 1),
(3, 'Programming Terms', 'Thuật ngữ lập trình cơ bản', 1);

-- Thêm thẻ học demo
INSERT IGNORE INTO cards (id, front, back, hint, deck_id) VALUES 
-- English Vocabulary
(1, 'Hello', 'Xin chào', 'Lời chào cơ bản nhất', 1),
(2, 'Thank you', 'Cảm ơn', 'Lời cảm ơn lịch sự', 1),
(3, 'Goodbye', 'Tạm biệt', 'Lời chào khi chia tay', 1),
(4, 'Please', 'Xin hãy/Làm ơn', 'Từ thể hiện sự lịch sự', 1),
(5, 'Sorry', 'Xin lỗi', 'Lời xin lỗi', 1),

-- TOEIC Words  
(6, 'Accomplish', 'Hoàn thành, đạt được', 'Đồng nghĩa: achieve, complete', 2),
(7, 'Achievement', 'Thành tựu, thành tích', 'Danh từ của achieve', 2),
(8, 'Analyze', 'Phân tích', 'Xem xét chi tiết', 2),
(9, 'Approach', 'Tiếp cận, phương pháp', 'Cách thức làm việc', 2),
(10, 'Benefit', 'Lợi ích', 'Advantage, profit', 2),

-- Programming Terms
(11, 'Variable', 'Biến', 'Lưu trữ dữ liệu', 3),
(12, 'Function', 'Hàm', 'Khối code có thể tái sử dụng', 3),
(13, 'Loop', 'Vòng lặp', 'Lặp lại một đoạn code', 3),
(14, 'Array', 'Mảng', 'Tập hợp các phần tử', 3),
(15, 'Object', 'Đối tượng', 'Thực thể trong lập trình', 3);

SELECT '✅ Database setup hoàn tất!' as message;
SELECT 'Tables created:' as info;
SHOW TABLES;