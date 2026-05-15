CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    item_id BIGINT NULL,
    reported_user_id BIGINT NULL,
    reason VARCHAR(120) NOT NULL,
    details VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_reports_item FOREIGN KEY (item_id) REFERENCES items(id),
    CONSTRAINT fk_reports_user FOREIGN KEY (reported_user_id) REFERENCES users(id)
);

CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_created_at ON reports(created_at);
