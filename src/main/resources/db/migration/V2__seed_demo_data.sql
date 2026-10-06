-- Development-only demo password for every account: password
INSERT INTO users (id, full_name, email, password_hash, phone, role, active, created_at, updated_at) VALUES
(1, 'Admin User', 'admin@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0711111111', 'RAILWAY_ADMIN', TRUE, '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(2, 'Booking Officer', 'officer@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0722222222', 'BOOKING_OFFICER', TRUE, '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, 'Nimal Perera', 'passenger@railconnect.lk', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0771234567', 'PASSENGER', TRUE, '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO trains (id, train_number, train_name, description, status, created_at, updated_at) VALUES
(1, '1001', 'Udarata Menike', 'Hill country intercity service', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(2, '1005', 'Podi Menike', 'Colombo to Kandy express service', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, '4001', 'Yal Devi', 'Northern line intercity service', 'MAINTENANCE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO routes (id, route_code, start_station, end_station, distance_km, status, created_at, updated_at) VALUES
(1, 'RT-CMB-KDY', 'Colombo Fort', 'Kandy', 120.70, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(2, 'RT-KDY-CMB', 'Kandy', 'Colombo Fort', 120.70, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, 'RT-CMB-GLE', 'Colombo Fort', 'Galle', 116.00, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO train_schedules (id, schedule_code, train_id, route_id, travel_date, departure_time, arrival_time, base_fare, status, created_at, updated_at) VALUES
(1, 'SCH-2026-001', 2, 1, '2026-10-12', '08:30:00', '11:15:00', 1200.00, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(2, 'SCH-2026-002', 1, 2, '2026-10-13', '13:45:00', '16:30:00', 1100.00, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, 'SCH-2026-003', 1, 1, '2026-10-14', '05:55:00', '08:45:00', 1250.00, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO carriages (id, train_id, carriage_number, class_type, capacity, status, created_at, updated_at) VALUES
(1, 2, 'A01', 'SECOND', 12, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(2, 2, 'B01', 'FIRST', 8, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, 1, 'A01', 'SECOND', 12, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO seats (id, carriage_id, seat_number, seat_type, status, created_at, updated_at) VALUES
(1, 1, '01', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (2, 1, '02', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(3, 1, '03', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (4, 1, '04', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(5, 1, '05', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (6, 1, '06', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(7, 1, '07', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (8, 1, '08', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(9, 1, '09', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (10, 1, '10', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(11, 1, '11', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (12, 1, '12', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(13, 2, '01', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (14, 2, '02', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(15, 2, '03', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (16, 2, '04', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(17, 2, '05', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (18, 2, '06', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(19, 2, '07', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (20, 2, '08', 'WINDOW', 'OUT_OF_SERVICE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(21, 3, '01', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (22, 3, '02', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(23, 3, '03', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (24, 3, '04', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(25, 3, '05', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (26, 3, '06', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(27, 3, '07', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (28, 3, '08', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(29, 3, '09', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (30, 3, '10', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'),
(31, 3, '11', 'AISLE', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'), (32, 3, '12', 'WINDOW', 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO ticket_bookings (id, booking_reference, user_id, schedule_id, passenger_name, contact_email, contact_phone, total_amount, status, hold_expires_at, created_at, updated_at) VALUES
(1, 'BK-DEMO1001', 3, 1, 'Nimal Perera', 'passenger@railconnect.lk', '0771234567', 1300.00, 'CONFIRMED', '2026-10-12 12:00:00.000', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO booking_seats (id, booking_id, seat_id, fare_amount, created_at, updated_at) VALUES
(1, 1, 1, 1200.00, '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO seat_reservations (id, schedule_id, seat_id, booking_id, status, expires_at, created_at, updated_at) VALUES
(1, 1, 1, 1, 'CONFIRMED', '2026-10-12 12:00:00.000', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO payments (id, booking_id, attempt_number, amount, status, method, transaction_reference, created_at, updated_at) VALUES
(1, 1, 1, 1300.00, 'SUCCEEDED', 'SIMULATED', 'SIM-DEMO-1001', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');

INSERT INTO complaints (id, complaint_reference, user_id, booking_id, complaint_type, subject, description, status, admin_response, created_at, updated_at) VALUES
(1, 'CMP-DEMO1001', 3, 1, 'SERVICE', 'Request for booking assistance', 'Please confirm the platform information for my journey.', 'OPEN', NULL, '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000');
