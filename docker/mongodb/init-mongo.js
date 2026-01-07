// MongoDB Initialization Script
// This file is automatically executed by MongoDB on first startup
// Place in: docker/mongodb/init-mongo.js

print('========================================');
print('Starting MongoDB Initialization...');
print('========================================');

// Wait a bit for MongoDB to be fully ready
sleep(2000);

// ============================================
// INVENTORY DATABASE
// ============================================
print('Setting up inventory_db...');

db = db.getSiblingDB('inventory_db');

// Create collections
db.createCollection('events');
db.createCollection('venues');
db.createCollection('reservations');
print('✓ Collections created: events, venues, reservations');

// Create indexes
db.events.createIndex({ 'id': 1 }, { unique: true, name: 'idx_event_id' });
db.events.createIndex({ 'name': 1 }, { name: 'idx_event_name' });
db.events.createIndex({ 'leftCapacity': 1 }, { name: 'idx_event_capacity' });
print('✓ Indexes created for events collection');

db.venues.createIndex({ 'id': 1 }, { unique: true, name: 'idx_venue_id' });
db.venues.createIndex({ 'name': 1 }, { name: 'idx_venue_name' });
print('✓ Indexes created for venues collection');

db.reservations.createIndex({ 'transactionId': 1 }, { unique: true, name: 'idx_reservation_txn' });
db.reservations.createIndex({ 'eventId': 1 }, { name: 'idx_reservation_event' });
db.reservations.createIndex({ 'userId': 1 }, { name: 'idx_reservation_user' });
db.reservations.createIndex({ 'status': 1 }, { name: 'idx_reservation_status' });
db.reservations.createIndex({ 'createdAt': 1 }, {
  expireAfterSeconds: 2592000,
  name: 'idx_reservation_ttl'
}); // TTL: 30 days
print('✓ Indexes created for reservations collection (including TTL)');

// Insert sample venues
try {
  db.venues.insertMany([
    {
      id: NumberLong(1),
      name: 'Madison Square Garden',
      address: '4 Pennsylvania Plaza, New York, NY 10001',
      totalCapacity: NumberLong(20000)
    },
    {
      id: NumberLong(2),
      name: 'Staples Center',
      address: '1111 S Figueroa St, Los Angeles, CA 90015',
      totalCapacity: NumberLong(21000)
    },
    {
      id: NumberLong(3),
      name: 'Red Rocks Amphitheatre',
      address: '18300 W Alameda Pkwy, Morrison, CO 80465',
      totalCapacity: NumberLong(9525)
    }
  ]);
  print('✓ Sample venues inserted: ' + db.venues.countDocuments());
} catch (e) {
  print('Venues may already exist: ' + e);
}

// Insert sample events with embedded venue data
try {
  db.events.insertMany([
    {
      id: NumberLong(1),
      name: 'Rock Concert 2024',
      totalCapacity: NumberLong(15000),
      leftCapacity: NumberLong(15000),
      ticketPrice: NumberDecimal('75.50'),
      venue: {
        mongoId: null,
        id: NumberLong(1),
        name: 'Madison Square Garden',
        address: '4 Pennsylvania Plaza, New York, NY 10001',
        totalCapacity: NumberLong(20000)
      }
    },
    {
      id: NumberLong(2),
      name: 'Basketball Game - Knicks vs Lakers',
      totalCapacity: NumberLong(18000),
      leftCapacity: NumberLong(18000),
      ticketPrice: NumberDecimal('120.00'),
      venue: {
        mongoId: null,
        id: NumberLong(1),
        name: 'Madison Square Garden',
        address: '4 Pennsylvania Plaza, New York, NY 10001',
        totalCapacity: NumberLong(20000)
      }
    },
    {
      id: NumberLong(3),
      name: 'Summer Music Festival',
      totalCapacity: NumberLong(9000),
      leftCapacity: NumberLong(9000),
      ticketPrice: NumberDecimal('85.00'),
      venue: {
        mongoId: null,
        id: NumberLong(3),
        name: 'Red Rocks Amphitheatre',
        address: '18300 W Alameda Pkwy, Morrison, CO 80465',
        totalCapacity: NumberLong(9525)
      }
    },
    {
      id: NumberLong(4),
      name: 'Jazz Night',
      totalCapacity: NumberLong(5000),
      leftCapacity: NumberLong(5000),
      ticketPrice: NumberDecimal('45.00'),
      venue: {
        mongoId: null,
        id: NumberLong(1),
        name: 'Madison Square Garden',
        address: '4 Pennsylvania Plaza, New York, NY 10001',
        totalCapacity: NumberLong(20000)
      }
    }
  ]);
  print('✓ Sample events inserted: ' + db.events.countDocuments());
} catch (e) {
  print('Events may already exist: ' + e);
}

print('✓ Inventory DB setup complete');
print('  - Events: ' + db.events.countDocuments());
print('  - Venues: ' + db.venues.countDocuments());

// ============================================
// BOOKING DATABASE
// ============================================
print('Setting up booking_db...');

db = db.getSiblingDB('booking_db');

// Create collections
db.createCollection('customers');
db.createCollection('bookings');
print('✓ Collections created: customers, bookings');

// Create indexes
db.customers.createIndex({ 'id': 1 }, { unique: true, name: 'idx_customer_id' });
db.customers.createIndex({ 'email': 1 }, { unique: true, name: 'idx_customer_email' });
print('✓ Indexes created for customers collection');

db.bookings.createIndex({ 'transactionId': 1 }, { unique: true, name: 'idx_booking_txn' });
db.bookings.createIndex({ 'userId': 1 }, { name: 'idx_booking_user' });
db.bookings.createIndex({ 'eventId': 1 }, { name: 'idx_booking_event' });
db.bookings.createIndex({ 'status': 1 }, { name: 'idx_booking_status' });
db.bookings.createIndex({ 'createdAt': 1 }, { name: 'idx_booking_created' });
print('✓ Indexes created for bookings collection');

// Insert sample customers
try {
  db.customers.insertMany([
    {
      id: NumberLong(1),
      name: 'John Doe',
      email: 'john.doe@example.com',
      address: '123 Main St, New York, NY 10001'
    },
    {
      id: NumberLong(2),
      name: 'Jane Smith',
      email: 'jane.smith@example.com',
      address: '456 Oak Ave, Los Angeles, CA 90015'
    },
    {
      id: NumberLong(3),
      name: 'Bob Johnson',
      email: 'bob.johnson@example.com',
      address: '789 Pine Rd, Morrison, CO 80465'
    }
  ]);
  print('✓ Sample customers inserted: ' + db.customers.countDocuments());
} catch (e) {
  print('Customers may already exist: ' + e);
}

print('✓ Booking DB setup complete');
print('  - Customers: ' + db.customers.countDocuments());

// ============================================
// ORDER DATABASE
// ============================================
print('Setting up order_db...');

db = db.getSiblingDB('order_db');

// Create collections
db.createCollection('orders');
print('✓ Collections created: orders');

// Create indexes
db.orders.createIndex({ 'transactionId': 1 }, { unique: true, name: 'idx_order_txn' });
db.orders.createIndex({ 'customerId': 1 }, { name: 'idx_order_customer' });
db.orders.createIndex({ 'eventId': 1 }, { name: 'idx_order_event' });
db.orders.createIndex({ 'status': 1 }, { name: 'idx_order_status' });
db.orders.createIndex({ 'placedAt': 1 }, { name: 'idx_order_placed' });
print('✓ Indexes created for orders collection');

print('✓ Order DB setup complete');

// ============================================
// SUMMARY
// ============================================
print('');
print('========================================');
print('MongoDB Initialization Complete!');
print('========================================');
print('Databases created:');
print('  ✓ inventory_db');
print('    - events: ' + db.getSiblingDB('inventory_db').events.countDocuments());
print('    - venues: ' + db.getSiblingDB('inventory_db').venues.countDocuments());
print('    - reservations: ready');
print('  ✓ booking_db');
print('    - customers: ' + db.getSiblingDB('booking_db').customers.countDocuments());
print('    - bookings: ready');
print('  ✓ order_db');
print('    - orders: ready');
print('');
print('Replica Set: rs0');
print('TTL Index: 30 days for reservations');
print('');
print('NOTE: No authentication configured');
print('Spring Boot can connect without credentials');
print('========================================');