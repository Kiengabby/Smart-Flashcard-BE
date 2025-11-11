-- Script tạo bảng quiz_sessions cho tính năng Quiz Recognition
-- Chạy script này trong database để tạo bảng mới

CREATE TABLE IF NOT EXISTS quiz_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_question INTEGER NOT NULL DEFAULT 1,
    total_questions INTEGER NOT NULL,
    card_ids TEXT,
    correct_answers INTEGER DEFAULT 0,
    wrong_answers INTEGER DEFAULT 0,
    correct_card_ids TEXT DEFAULT '[]',
    wrong_card_ids TEXT DEFAULT '[]',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_quiz_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_session_deck FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    
    -- Indexes for better performance
    INDEX idx_quiz_session_user_deck (user_id, deck_id),
    INDEX idx_quiz_session_status (status),
    INDEX idx_quiz_session_completed_at (completed_at)
);

-- Thêm comment cho bảng
ALTER TABLE quiz_sessions COMMENT = 'Bảng lưu trữ session quiz của người dùng';

-- Verify bảng đã được tạo
SHOW CREATE TABLE quiz_sessions;