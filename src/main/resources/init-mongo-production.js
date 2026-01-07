// MongoDB Initialization Script for SAGA Pattern Support
// This script runs when the MongoDB container starts for the first time

// Create databases and users for each microservice

// Switch to admin database to create users
db = db.getSiblingDB('admin');

// ============================================
// INVENTORY SERVICE DATABASE (SAGA Support)
// ============================================
db = db.getSiblingDB('inventory_db');
db.createUser({
  user: 'dev123',
  pwd: 'dev123',
  roles: [
    {
      role: 'readWrite',
      db: 'inventory_db'
    }
  ]
});

// Create collections with indexes
db.createCollection('events');
db.createCollection('venues');
db.createCollection('reservations');  // SAGA compensation tracking

// Create indexes for events
db.events.createIndex({ 'id': 1 }, { unique: true });
db.events.createIndex({ 'name': 1 });
db.events.createIndex({ 'leftCapacity': 1 });

// Create indexes for venues
db.venues.createIndex({ 'id': 1 }, { unique: true });
db.venues.createIndex({ 'name': 1 });

// SAGA PATTERN: Create indexes for reservations (for compensation logic)
// These track temporary reservations that may need rollback
db.reservations.createIndex({ 'transactionId': 1 }, { unique: true });
db.reservations.createIndex({ 'eventId': 1 });
db.reservations.createIndex({ 'status': 1 });
db.reservations.createIndex({ 'createdAt': 1 }, { expireAfterSeconds: 86400 }); // TTL index: 24 hours

// ============================================
// BOOKING SERVICE DATABASE (SAGA Support)
// ============================================
db = db.getSiblingDB('booking_db');
db.createUser({
  user: 'dev123',
  pwd: 'dev123',
  roles: [
    {
      role: 'readWrite',
      db: 'booking_db'
    }
  ]
});

// Create collections with indexes
db.createCollection('customers');
db.createCollection('bookings');  // SAGA transaction tracking

// Create indexes for customers
db.customers.createIndex({ 'id': 1 }, { unique: true });
db.customers.createIndex({ 'email': 1 }, { unique: true });

// SAGA PATTERN: Create indexes for bookings
db.bookings.createIndex({ 'transactionId': 1 }, { unique: true });
db.bookings.createIndex({ 'userId': 1 });
db.bookings.createIndex({ 'eventId': 1 });
db.bookings.createIndex({ 'createdAt': 1 });
db.bookings.createIndex({ 'status': 1 });  // For SAGA state tracking (PENDING, CONFIRMED, COMPENSATED)

// ============================================
// ORDER SERVICE DATABASE (SAGA Support)
// ============================================
db = db.getSiblingDB('order_db');
db.createUser({
  user: 'dev123',
  pwd: 'dev123',
  roles: [
    {
      role: 'readWrite',
      db: 'order_db'
    }
  ]
});

// Create collections with indexes
db.createCollection('orders');

// SAGA PATTERN: Create indexes for orders
db.orders.createIndex({ 'id': 1 });
db.orders.createIndex({ 'transactionId': 1 }, { unique: true });
db.orders.createIndex({ 'customerId': 1 });
db.orders.createIndex({ 'eventId': 1 });
db.orders.createIndex({ 'placedAt': 1 });
db.orders.createIndex({ 'status': 1 });  // For SAGA state tracking (PENDING, CONFIRMED, CANCELLED)

// ============================================
// SAMPLE DATA FOR TESTING SAGA PATTERN
// ============================================
db = db.getSiblingDB('inventory_db');

// Sample venues
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

// Sample events with SAGA-friendly structure
db.events.insertMany([
  {
    id: NumberLong(1),
    name: 'Rock Concert 2024',
    totalCapacity: NumberLong(15000),
    leftCapacity: NumberLong(15000),
    ticketPrice: NumberDecimal('75.50'),
    venue: {
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
      id: NumberLong(1),
      name: 'Madison Square Garden',
      address: '4 Pennsylvania Plaza, New York, NY 10001',
      totalCapacity: NumberLong(20000)
    }
  }
]);

// Sample customers in booking_db
db = db.getSiblingDB('booking_db');
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

print('==============================================');
print('MongoDB SAGA Pattern initialization completed!');
print('==============================================');
print('Databases created:');
print('  - inventory_db (events, venues, reservations)');
print('  - booking_db (customers, bookings)');
print('  - order_db (orders)');
print('');
print('Replica Set: rs0');
print('Transaction support: ENABLED');
print('SAGA Pattern: READY');
print('==============================================');