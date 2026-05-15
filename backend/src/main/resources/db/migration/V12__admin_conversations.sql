ALTER TABLE conversations MODIFY COLUMN item_id BIGINT NULL;
ALTER TABLE conversations ADD COLUMN admin_conversation BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE conversations SET admin_conversation = FALSE WHERE admin_conversation IS NULL;
