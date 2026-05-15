CREATE TABLE chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    read_by_recipient BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_messages_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_chat_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users(id)
);

CREATE INDEX idx_chat_messages_reservation_created ON chat_messages(reservation_id, created_at);
CREATE INDEX idx_chat_messages_recipient_read ON chat_messages(recipient_id, read_by_recipient);
