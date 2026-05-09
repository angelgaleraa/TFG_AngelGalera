CREATE DATABASE IF NOT EXISTS rental_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rental_platform;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(255) NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    city VARCHAR(255) NOT NULL,
    municipality VARCHAR(120) NOT NULL,
    active BIT(1) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    hidden_by_renter BIT(1) NOT NULL DEFAULT 0,
    hidden_by_owner BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_reservations_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_reservations_renter FOREIGN KEY (renter_id) REFERENCES users(id)
);

CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT,
    reservation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    read_by_recipient BIT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_chat_messages_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_chat_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT fk_chat_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users(id)
);

CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    reservation_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_conversations_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_conversations_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_renter FOREIGN KEY (renter_id) REFERENCES users(id),
    CONSTRAINT fk_conversations_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE INDEX idx_items_owner ON items(owner_id);
CREATE INDEX idx_reservations_item ON reservations(item_id);
CREATE INDEX idx_reservations_renter ON reservations(renter_id);
CREATE INDEX idx_chat_messages_reservation_created ON chat_messages(reservation_id, created_at);
CREATE INDEX idx_chat_messages_recipient_read ON chat_messages(recipient_id, read_by_recipient);
CREATE INDEX idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at);
CREATE UNIQUE INDEX idx_conversations_item_renter ON conversations(item_id, renter_id);
