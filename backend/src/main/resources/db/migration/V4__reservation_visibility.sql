ALTER TABLE reservations ADD COLUMN hidden_by_renter BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN hidden_by_owner BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_reservation_renter_visible ON reservations(renter_id, hidden_by_renter);
CREATE INDEX idx_reservation_owner_visible ON reservations(item_id, hidden_by_owner);
