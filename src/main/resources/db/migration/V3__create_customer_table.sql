CREATE TABLE  customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL
);



--CREATE INDEX idx_event_venue_id ON event(venue_id);
--CREATE INDEX idx_venue_name ON venue(name);
--CREATE INDEX idx_event_name ON event(name);