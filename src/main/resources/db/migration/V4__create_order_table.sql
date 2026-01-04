
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total DECIMAL(10,2) NOT NULL,
    quantity BIGINT NOT NULL,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    customer_id BIGINT,
    event_id BIGINT ,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE SET NULL
);

--CREATE INDEX idx_event_venue_id ON event(venue_id);
--CREATE INDEX idx_venue_name ON venue(name);
--CREATE INDEX idx_event_name ON event(name);