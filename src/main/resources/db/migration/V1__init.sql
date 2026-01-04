CREATE TABLE IF NOT EXISTS venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    total_capacity BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_capacity BIGINT NOT NULL,
    left_capacity BIGINT NOT NULL,
    venue_id BIGINT NOT NULL,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE
);

--CREATE INDEX idx_event_venue_id ON event(venue_id);
--CREATE INDEX idx_venue_name ON venue(name);
--CREATE INDEX idx_event_name ON event(name);