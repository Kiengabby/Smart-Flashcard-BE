-- Smart Flashcard Database Setup for MySQL
-- Đồ án tốt nghiệp - Hệ thống học từ vựng với thẻ ghi nhớ

-- Tạo database
CREATE DATABASE IF NOT EXISTS smart_flashcard 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE smart_flashcard;

-- Bảng users (người dùng)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email)
);

-- Bảng decks (bộ thẻ)
CREATE TABLE IF NOT EXISTS decks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- Bảng cards (thẻ học)
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    deck_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- SM-2 Algorithm fields
    ease_factor DECIMAL(4,2) DEFAULT 2.50,
    repetition INT DEFAULT 0,
    interval_days INT DEFAULT 1,
    next_review_date DATE DEFAULT (CURDATE()),
    
    FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    INDEX idx_deck_id (deck_id),
    INDEX idx_next_review (next_review_date)
);

-- Bảng reviews (lịch sử ôn tập)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quality INT NOT NULL CHECK (quality >= 0 AND quality <= 5),
    reviewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_card_user (card_id, user_id),
    INDEX idx_reviewed_at (reviewed_at)
);

-- Insert sample data for demo
INSERT INTO users (email, password, display_name) VALUES 
('demo@smartflashcard.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Demo User'),
('student@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIjbVv0VMerX9G', 'Sinh viên Demo');
-- Password for both users: "password123"

INSERT INTO decks (name, description, user_id) VALUES 
('English Vocabulary', 'Từ vựng tiếng Anh cơ bản', 1),
('TOEIC Words', 'Từ vựng TOEIC thường gặp', 1),
('Daily Conversation', 'Giao tiếp hàng ngày', 2);

INSERT INTO cards (front, back, deck_id) VALUES 
('Hello', 'Xin chào', 1),
('Thank you', 'Cảm ơn', 1),
('Goodbye', 'Tạm biệt', 1),
('Accomplish', 'Hoàn thành, đạt được', 2),
('Achievement', 'Thành tựu, thành tích', 2),
('How are you?', 'Bạn có khỏe không?', 3),
('Nice to meet you', 'Rất vui được gặp bạn', 3);

-- Tạo view để dễ dàng query
CREATE VIEW deck_summary AS
SELECT 
    d.id,
    d.name,
    d.description,
    d.user_id,
    u.display_name as owner_name,
    COUNT(c.id) as card_count,
    d.created_at,
    d.updated_at
FROM decks d
LEFT JOIN cards c ON d.id = c.deck_id
LEFT JOIN users u ON d.user_id = u.id
GROUP BY d.id, d.name, d.description, d.user_id, u.display_name, d.created_at, d.updated_at;

-- Hiển thị thông tin database đã tạo
SELECT 'Database smart_flashcard created successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as deck_count FROM decks;
SELECT COUNT(*) as card_count FROM cards;
