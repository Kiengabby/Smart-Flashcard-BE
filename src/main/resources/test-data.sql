-- Insert test user
INSERT INTO users (email, password, display_name, created_at, updated_at)
VALUES ('test@example.com', '$2a$10$dXJ3SW6G7P1lWBQOQOoWe.Q1lApCVyQ8zqF2hJ8Y8zU7zK2tGl7MG', 'Test User', NOW(), NOW())
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name);

-- Insert test deck
INSERT INTO decks (name, description, language, user_id, created_at, updated_at)
VALUES ('Vocabulary Test', 'Test deck for daily review', 'vi', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert test cards
INSERT INTO cards (front, back, deck_id, created_at, updated_at)
VALUES 
('Hello', 'Xin chào', 1, NOW(), NOW()),
('Goodbye', 'Tạm biệt', 1, NOW(), NOW()),
('Thank you', 'Cảm ơn', 1, NOW(), NOW()),
('Please', 'Xin lời', 1, NOW(), NOW()),
('How are you?', 'Bạn có khỏe không?', 1, NOW(), NOW()),
('Good morning', 'Chào buổi sáng', 1, NOW(), NOW()),
('Good evening', 'Chào buổi tối', 1, NOW(), NOW()),
('Yes', 'Vâng/Có', 1, NOW(), NOW()),
('No', 'Không', 1, NOW(), NOW()),
('Water', 'Nước', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE front = VALUES(front);

-- Insert spaced repetition records with some cards due for review
INSERT INTO spaced_repetition (user_id, card_id, easiness_factor, repetitions, interval_days, 
                              next_review_date, total_reviews, successful_reviews, learning_phase, 
                              difficulty_level, created_at, updated_at)
SELECT 
    1 as user_id,
    c.id as card_id,
    2.5 as easiness_factor,
    0 as repetitions,
    1 as interval_days,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as next_review_date, -- Make cards due for review
    0 as total_reviews,
    0 as successful_reviews,
    'NEW' as learning_phase,
    'MEDIUM' as difficulty_level,
    NOW() as created_at,
    NOW() as updated_at
FROM cards c 
WHERE c.deck_id = 1
ON DUPLICATE KEY UPDATE 
    next_review_date = VALUES(next_review_date),
    learning_phase = VALUES(learning_phase);

-- Update some cards to be due today and some overdue
UPDATE spaced_repetition 
SET next_review_date = DATE_SUB(NOW(), INTERVAL 2 DAY),
    learning_phase = 'LEARNING',
    total_reviews = 2,
    successful_reviews = 1,
    repetitions = 1
WHERE card_id IN (1, 2, 3);

UPDATE spaced_repetition 
SET next_review_date = DATE_SUB(NOW(), INTERVAL 1 HOUR),
    learning_phase = 'REVIEW',
    total_reviews = 5,
    successful_reviews = 4,
    repetitions = 3
WHERE card_id IN (4, 5, 6);

-- Set some cards as due today
UPDATE spaced_repetition 
SET next_review_date = NOW(),
    learning_phase = 'NEW'
WHERE card_id IN (7, 8, 9, 10);
