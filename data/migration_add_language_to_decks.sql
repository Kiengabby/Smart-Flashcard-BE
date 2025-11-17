-- Add language column to decks table
ALTER TABLE decks ADD COLUMN language VARCHAR(10) DEFAULT 'en' AFTER description;

-- Update existing decks with default language
UPDATE decks SET language = 'en' WHERE language IS NULL;

-- Add index for better query performance  
CREATE INDEX idx_decks_language ON decks(language);