-- H2 Database syntax (không support AFTER clause)
ALTER TABLE quiz_sessions ADD COLUMN current_options TEXT;
ALTER TABLE quiz_sessions ADD COLUMN current_correct_answer_index INT;