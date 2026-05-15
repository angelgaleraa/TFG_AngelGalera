ALTER TABLE items ADD COLUMN owner_removed BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE items SET owner_removed = FALSE WHERE owner_removed IS NULL;
