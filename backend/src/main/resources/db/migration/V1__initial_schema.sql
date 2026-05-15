CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(30),
    bio VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(120) NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    city VARCHAR(120) NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_items_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    renter_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    platform_fee_percent DECIMAL(5,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reservations_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_reservations_renter FOREIGN KEY (renter_id) REFERENCES users(id)
);

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    payer_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    method VARCHAR(40) NOT NULL,
    reference_code VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_payments_payer FOREIGN KEY (payer_id) REFERENCES users(id),
    CONSTRAINT fk_payments_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    message VARCHAR(350) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reviews_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_reviews_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_reviews_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE INDEX idx_items_owner_id ON items(owner_id);
CREATE INDEX idx_items_active ON items(active);
CREATE INDEX idx_reservation_item_dates ON reservations(item_id, start_date, end_date);
CREATE INDEX idx_reservation_renter ON reservations(renter_id);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read_flag);
CREATE INDEX idx_reviews_item ON reviews(item_id);
