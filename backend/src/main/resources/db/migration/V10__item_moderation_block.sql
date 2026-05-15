ALTER TABLE items ADD COLUMN moderation_blocked BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE items SET moderation_blocked = FALSE WHERE moderation_blocked IS NULL;
