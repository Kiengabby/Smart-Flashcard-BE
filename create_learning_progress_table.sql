-- Migration: Create learning_progress table
-- Description: Track user's overall learning progress for each deck across 4 modes

CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    
    -- Flashcard mode
    flashcard_completed BOOLEAN NOT NULL DEFAULT FALSE,
    flashcard_score INT,
    flashcard_completed_at TIMESTAMP NULL,
    
    -- Quiz mode
    quiz_completed BOOLEAN NOT NULL DEFAULT FALSE,
    quiz_score INT,
    quiz_completed_at TIMESTAMP NULL,
    
    -- Listening mode
    listening_completed BOOLEAN NOT NULL DEFAULT FALSE,
    listening_score INT,
    listening_completed_at TIMESTAMP NULL,
    
    -- Writing mode
    writing_completed BOOLEAN NOT NULL DEFAULT FALSE,
    writing_score INT,
    writing_completed_at TIMESTAMP NULL,
    
    -- Overall progress
    overall_progress INT NOT NULL DEFAULT 0,
    is_fully_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    status INT NOT NULL DEFAULT 1,
    
    -- Foreign keys
    CONSTRAINT fk_learning_progress_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_learning_progress_deck 
        FOREIGN KEY (deck_id) REFERENCES decks(id) ON DELETE CASCADE,
    
    -- Unique constraint: One progress record per user per deck
    CONSTRAINT uk_user_deck UNIQUE (user_id, deck_id)
);

-- Indexes for performance
CREATE INDEX idx_learning_progress_user_deck ON learning_progress(user_id, deck_id);
CREATE INDEX idx_learning_progress_deck ON learning_progress(deck_id);
CREATE INDEX idx_learning_progress_completed ON learning_progress(is_fully_completed, completed_at);
CREATE INDEX idx_learning_progress_updated ON learning_progress(updated_at);

-- Comments
ALTER TABLE learning_progress COMMENT = 'Tracks overall learning progress for decks across 4 learning modes';
