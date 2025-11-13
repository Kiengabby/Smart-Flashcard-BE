-- Add current_options and current_correct_answer_index columns to quiz_sessions table
-- To fix quiz answer logic by storing shuffled options and correct index

ALTER TABLE quiz_sessions ADD COLUMN current_options TEXT AFTER card_ids;
ALTER TABLE quiz_sessions ADD COLUMN current_correct_answer_index INT AFTER current_options;