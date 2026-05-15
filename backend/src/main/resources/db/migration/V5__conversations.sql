CREATE TABLE conversations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    reservation_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_conversations_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_conversations_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_renter FOREIGN KEY (renter_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

ALTER TABLE chat_messages ADD COLUMN conversation_id BIGINT;

INSERT INTO conversations (item_id, owner_id, renter_id, reservation_id, created_at, updated_at)
SELECT r.item_id, i.owner_id, r.renter_id, r.id, r.created_at, r.updated_at
FROM reservations r
JOIN items i ON i.id = r.item_id
WHERE EXISTS (
    SELECT 1
    FROM chat_messages cm
    WHERE cm.reservation_id = r.id
);

UPDATE chat_messages cm
SET conversation_id = (
    SELECT c.id
    FROM conversations c
    WHERE c.reservation_id = cm.reservation_id
);

ALTER TABLE chat_messages ADD CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id);

CREATE UNIQUE INDEX idx_conversations_item_renter ON conversations(item_id, renter_id);
CREATE INDEX idx_conversations_owner_updated ON conversations(owner_id, updated_at);
CREATE INDEX idx_conversations_renter_updated ON conversations(renter_id, updated_at);
CREATE INDEX idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at);
