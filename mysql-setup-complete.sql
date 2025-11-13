-- =============================================================================
-- SMART FLASHCARD DATABASE SETUP - MYSQL
-- Đồ án tốt nghiệp - Hệ thống học từ vựng thông minh
-- Author: Kiên - Smart Flashcard Team
-- =============================================================================

-- Tạo database với charset UTF-8 để hỗ trợ tiếng Việt
DROP DATABASE IF EXISTS smart_flashcard;
CREATE DATABASE smart_flashcard 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE smart_flashcard;

-- =============================================================================
-- 1. BẢNG USERS (NGƯỜI DÙNG)
-- =============================================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 2. BẢNG DECKS (BỘ THẺ HỌC)
-- =============================================================================
CREATE TABLE decks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 3. BẢNG CARDS (THẺ HỌC)
-- =============================================================================
CREATE TABLE cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    hint TEXT,
    image_url VARCHAR(500),
    audio_url VARCHAR(500),
    deck_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    INDEX idx_deck_id (deck_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 4. BẢNG USER_CARD_PROGRESS (TIẾN ĐỘ HỌC TẬP SM-2)
-- =============================================================================
CREATE TABLE user_card_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    repetitions INT DEFAULT 0,
    ease_factor DOUBLE DEFAULT 2.5,
    review_interval INT DEFAULT 0,
    last_reviewed_date DATE,
    next_review_date DATE,
    total_reviews INT DEFAULT 0,
    correct_reviews INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_next_review (next_review_date),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 5. BẢNG QUIZ_SESSIONS (PHIÊN QUIZ)
-- =============================================================================
CREATE TABLE quiz_sessions (
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
    correct_card_ids TEXT DEFAULT '[]',
    wrong_card_ids TEXT DEFAULT '[]',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    INDEX idx_user_deck (user_id, deck_id),
    INDEX idx_status (status),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 6. DỮ LIỆU MẪU CHO DEMO
-- =============================================================================

-- Tạo user demo
-- Password: "password123" đã được mã hóa bằng BCrypt
INSERT INTO users (email, password, display_name) VALUES 
('kiengabby@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Kiên - Smart Flashcard'),
('demo@smartflashcard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Demo User'),
('student@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Sinh viên Demo');

-- Tạo các bộ thẻ demo
INSERT INTO decks (name, description, user_id) VALUES 
('English Vocabulary', 'Từ vựng tiếng Anh cơ bản cho người mới bắt đầu', 1),
('TOEIC Essential Words', 'Từ vựng TOEIC cần thiết cho kỳ thi', 1),
('Daily English Conversation', 'Giao tiếp tiếng Anh hàng ngày', 1),
('Programming Terms', 'Thuật ngữ lập trình cơ bản', 2),
('Business English', 'Tiếng Anh thương mại', 2);

-- Tạo thẻ học demo
INSERT INTO cards (front, back, hint, deck_id) VALUES 
-- English Vocabulary
('Hello', 'Xin chào', 'Lời chào cơ bản nhất', 1),
('Thank you', 'Cảm ơn', 'Lời cảm ơn lịch sự', 1),
('Goodbye', 'Tạm biệt', 'Lời chào khi chia tay', 1),
('Please', 'Xin hãy/Làm ơn', 'Từ thể hiện sự lịch sự', 1),
('Sorry', 'Xin lỗi', 'Lời xin lỗi', 1),

-- TOEIC Words
('Accomplish', 'Hoàn thành, đạt được', 'Đồng nghĩa: achieve, complete', 2),
('Achievement', 'Thành tựu, thành tích', 'Danh từ của achieve', 2),
('Analyze', 'Phân tích', 'Xem xét chi tiết', 2),
('Approach', 'Tiếp cận, phương pháp', 'Cách thức làm việc', 2),
('Benefit', 'Lợi ích', 'Advantage, profit', 2),

-- Daily Conversation
('How are you?', 'Bạn có khỏe không?', 'Câu hỏi thăm hỏi sức khỏe', 3),
('Nice to meet you', 'Rất vui được gặp bạn', 'Khi gặp lần đầu', 3),
('What time is it?', 'Mấy giờ rồi?', 'Hỏi về thời gian', 3),
('Where are you from?', 'Bạn đến từ đâu?', 'Hỏi về quê quán', 3),
('How much is this?', 'Cái này giá bao nhiêu?', 'Hỏi giá cả', 3),

-- Programming Terms
('Variable', 'Biến', 'Lưu trữ dữ liệu', 4),
('Function', 'Hàm', 'Khối code có thể tái sử dụng', 4),
('Loop', 'Vòng lặp', 'Lặp lại một đoạn code', 4),
('Array', 'Mảng', 'Tập hợp các phần tử', 4),
('Object', 'Đối tượng', 'Thực thể trong lập trình', 4),

-- Business English
('Meeting', 'Cuộc họp', 'Gathering for discussion', 5),
('Schedule', 'Lịch trình', 'Timetable of activities', 5),
('Deadline', 'Hạn chót', 'Final date for completion', 5),
('Budget', 'Ngân sách', 'Financial plan', 5),
('Revenue', 'Doanh thu', 'Income from sales', 5);

-- =============================================================================
-- 7. TẠO VIEWS HỖ TRỢ QUERY
-- =============================================================================

-- View thống kê bộ thẻ
CREATE VIEW deck_stats AS
SELECT 
    d.id,
    d.name,
    d.description,
    d.user_id,
    u.display_name as owner_name,
    COUNT(c.id) as total_cards,
    d.created_at,
    d.updated_at
FROM decks d
LEFT JOIN cards c ON d.id = c.deck_id
LEFT JOIN users u ON d.user_id = u.id
GROUP BY d.id, d.name, d.description, d.user_id, u.display_name, d.created_at, d.updated_at;

-- View tiến độ học tập
CREATE VIEW study_progress AS
SELECT 
    u.id as user_id,
    u.display_name,
    COUNT(DISTINCT ucp.card_id) as cards_studied,
    AVG(ucp.ease_factor) as avg_ease_factor,
    COUNT(CASE WHEN ucp.next_review_date <= CURDATE() THEN 1 END) as cards_due_today
FROM users u
LEFT JOIN user_card_progress ucp ON u.id = ucp.user_id
GROUP BY u.id, u.display_name;

-- =============================================================================
-- 8. KIỂM TRA KẾT QUẢ
-- =============================================================================

SELECT '🎉 Database Smart Flashcard được tạo thành công!' as status;
SELECT '📊 THỐNG KÊ DATABASE:' as info;
SELECT 'Users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'Decks' as table_name, COUNT(*) as record_count FROM decks
UNION ALL
SELECT 'Cards' as table_name, COUNT(*) as record_count FROM cards;

SELECT '✅ Sẵn sàng để kết nối với Spring Boot!' as message;