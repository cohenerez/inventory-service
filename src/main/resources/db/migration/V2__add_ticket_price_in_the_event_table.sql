ALTER TABLE event
ADD COLUMN ticket_price DECIMAL(10,2) NOT NULL DEFAULT 10.00;

--CREATE INDEX idx_event_venue_id ON event(venue_id);
--CREATE INDEX idx_venue_name ON venue(name);
--CREATE INDEX idx_event_name ON event(name);