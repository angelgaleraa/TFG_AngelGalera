ALTER TABLE reviews
    ADD COLUMN target_user_id BIGINT NULL;

UPDATE reviews
SET target_user_id = (SELECT owner_id FROM items WHERE items.id = reviews.item_id)
WHERE target_user_id IS NULL AND item_id IS NOT NULL;

ALTER TABLE reviews
    MODIFY COLUMN reservation_id BIGINT NULL;

ALTER TABLE reviews
    MODIFY COLUMN item_id BIGINT NULL;

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_target_user FOREIGN KEY (target_user_id) REFERENCES users(id);

CREATE INDEX idx_reviews_target_user ON reviews(target_user_id);
