-- Development-only demo password for every account: password
INSERT INTO users (id, full_name, email, password_hash, phone, role, active, created_at, updated_at) VALUES
(1, 'Admin User', 'admin@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0711111111', 'RAILWAY_ADMIN', TRUE, NOW(), NOW()),
(2, 'Booking Officer', 'officer@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0722222222', 'BOOKING_OFFICER', TRUE, NOW(), NOW()),
(3, 'Nimal Perera', 'passenger@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0771234567', 'PASSENGER', TRUE, NOW(), NOW());

INSERT INTO trains (id, train_number, train_name, description, status, created_at, updated_at) VALUES
(1, '1001', 'Udarata Menike', 'Hill country intercity service', 'ACTIVE', NOW(), NOW()),
(2, '1005', 'Podi Menike', 'Colombo to Kandy express service', 'ACTIVE', NOW(), NOW()),
(3, '4001', 'Yal Devi', 'Northern line intercity service', 'MAINTENANCE', NOW(), NOW());

INSERT INTO routes (id, route_code, start_station, end_station, distance_km, status, created_at, updated_at) VALUES
(1, 'RT-CMB-KDY', 'Colombo Fort', 'Kandy', 120.70, 'ACTIVE', NOW(), NOW()),
(2, 'RT-KDY-CMB', 'Kandy', 'Colombo Fort', 120.70, 'ACTIVE', NOW(), NOW()),
(3, 'RT-CMB-GLE', 'Colombo Fort', 'Galle', 116.00, 'ACTIVE', NOW(), NOW());

INSERT INTO train_schedules (id, schedule_code, train_id, route_id, travel_date, departure_time, arrival_time, base_fare, status, created_at, updated_at) VALUES
(1, 'SCH-2026-001', 2, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '08:30:00', '11:15:00', 1200.00, 'ACTIVE', NOW(), NOW()),
(2, 'SCH-2026-002', 1, 2, DATE_ADD(CURDATE(), INTERVAL 8 DAY), '13:45:00', '16:30:00', 1100.00, 'ACTIVE', NOW(), NOW()),
(3, 'SCH-2026-003', 1, 1, DATE_ADD(CURDATE(), INTERVAL 9 DAY), '05:55:00', '08:45:00', 1250.00, 'ACTIVE', NOW(), NOW());

INSERT INTO carriages (id, train_id, carriage_number, class_type, capacity, status, created_at, updated_at) VALUES
(1, 2, 'A01', 'SECOND', 12, 'ACTIVE', NOW(), NOW()),
(2, 2, 'B01', 'FIRST', 8, 'ACTIVE', NOW(), NOW()),
(3, 1, 'A01', 'SECOND', 12, 'ACTIVE', NOW(), NOW());

INSERT INTO seats (id, carriage_id, seat_number, seat_type, status, created_at, updated_at) VALUES
(1, 1, '01', 'WINDOW', 'ACTIVE', NOW(), NOW()), (2, 1, '02', 'AISLE', 'ACTIVE', NOW(), NOW()),
(3, 1, '03', 'AISLE', 'ACTIVE', NOW(), NOW()), (4, 1, '04', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(5, 1, '05', 'WINDOW', 'ACTIVE', NOW(), NOW()), (6, 1, '06', 'AISLE', 'ACTIVE', NOW(), NOW()),
(7, 1, '07', 'AISLE', 'ACTIVE', NOW(), NOW()), (8, 1, '08', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(9, 1, '09', 'WINDOW', 'ACTIVE', NOW(), NOW()), (10, 1, '10', 'AISLE', 'ACTIVE', NOW(), NOW()),
(11, 1, '11', 'AISLE', 'ACTIVE', NOW(), NOW()), (12, 1, '12', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(13, 2, '01', 'WINDOW', 'ACTIVE', NOW(), NOW()), (14, 2, '02', 'AISLE', 'ACTIVE', NOW(), NOW()),
(15, 2, '03', 'AISLE', 'ACTIVE', NOW(), NOW()), (16, 2, '04', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(17, 2, '05', 'WINDOW', 'ACTIVE', NOW(), NOW()), (18, 2, '06', 'AISLE', 'ACTIVE', NOW(), NOW()),
(19, 2, '07', 'AISLE', 'ACTIVE', NOW(), NOW()), (20, 2, '08', 'WINDOW', 'OUT_OF_SERVICE', NOW(), NOW()),
(21, 3, '01', 'WINDOW', 'ACTIVE', NOW(), NOW()), (22, 3, '02', 'AISLE', 'ACTIVE', NOW(), NOW()),
(23, 3, '03', 'AISLE', 'ACTIVE', NOW(), NOW()), (24, 3, '04', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(25, 3, '05', 'WINDOW', 'ACTIVE', NOW(), NOW()), (26, 3, '06', 'AISLE', 'ACTIVE', NOW(), NOW()),
(27, 3, '07', 'AISLE', 'ACTIVE', NOW(), NOW()), (28, 3, '08', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(29, 3, '09', 'WINDOW', 'ACTIVE', NOW(), NOW()), (30, 3, '10', 'AISLE', 'ACTIVE', NOW(), NOW()),
(31, 3, '11', 'AISLE', 'ACTIVE', NOW(), NOW()), (32, 3, '12', 'WINDOW', 'ACTIVE', NOW(), NOW());

INSERT INTO ticket_bookings (id, booking_reference, user_id, schedule_id, passenger_name, contact_email, contact_phone, total_amount, status, hold_expires_at, created_at, updated_at) VALUES
(1, 'BK-DEMO1001', 3, 1, 'Nimal Perera', 'passenger@railconnect.lk', '0771234567', 1300.00, 'CONFIRMED', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW());

INSERT INTO booking_seats (id, booking_id, seat_id, fare_amount, created_at, updated_at) VALUES
(1, 1, 1, 1200.00, NOW(), NOW());

INSERT INTO seat_reservations (id, schedule_id, seat_id, booking_id, status, expires_at, created_at, updated_at) VALUES
(1, 1, 1, 1, 'CONFIRMED', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW());

INSERT INTO payments (id, booking_id, attempt_number, amount, status, method, transaction_reference, created_at, updated_at) VALUES
(1, 1, 1, 1300.00, 'SUCCEEDED', 'SIMULATED', 'SIM-DEMO-1001', NOW(), NOW());

INSERT INTO complaints (id, complaint_reference, user_id, booking_id, complaint_type, subject, description, status, admin_response, created_at, updated_at) VALUES
(1, 'CMP-DEMO1001', 3, 1, 'SERVICE', 'Request for booking assistance', 'Please confirm the platform information for my journey.', 'OPEN', NULL, NOW(), NOW());
