-- Development-only seed data for a more useful demonstration dataset.
-- This migration adds variety without changing the six assessed modules.

INSERT INTO trains (
    train_number, train_name, description, status,
    first_class_fare, second_class_fare, third_class_fare,
    created_at, updated_at
)
SELECT '2001', 'Ruhunu Kumari', 'Southern line intercity service', 'ACTIVE',
       2200.00, 1500.00, 1050.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number = '2001');

INSERT INTO trains (
    train_number, train_name, description, status,
    first_class_fare, second_class_fare, third_class_fare,
    created_at, updated_at
)
SELECT '3001', 'Intercity Express', 'Fast Colombo to Kandy service', 'ACTIVE',
       2500.00, 1700.00, 1200.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number = '3001');

INSERT INTO trains (
    train_number, train_name, description, status,
    first_class_fare, second_class_fare, third_class_fare,
    created_at, updated_at
)
SELECT '5001', 'Denuwara Menike', 'Central highlands service', 'ACTIVE',
       2000.00, 1350.00, 950.00, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM trains WHERE train_number = '5001');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'A01', 'SECOND', 16, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '2001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'A01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'B01', 'THIRD', 20, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '2001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'B01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'A01', 'FIRST', 8, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '3001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'A01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'B01', 'SECOND', 16, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '3001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'B01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'C01', 'THIRD', 16, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '3001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'C01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'A01', 'SECOND', 20, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '5001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'A01');

INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, 'B01', 'THIRD', 20, 'ACTIVE', NOW(), NOW()
FROM trains t
WHERE t.train_number = '5001'
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = 'B01');

-- Generate physical seats for each newly seeded carriage. Physical seat status is
-- intentionally separate from per-schedule reservation status.
INSERT IGNORE INTO seats (carriage_id, seat_number, seat_type, status, created_at, updated_at)
SELECT c.id,
       LPAD(numbers.n, 2, '0'),
       CASE WHEN MOD(numbers.n, 4) IN (0, 1) THEN 'WINDOW' ELSE 'AISLE' END,
       'ACTIVE', NOW(), NOW()
FROM carriages c
JOIN trains t ON t.id = c.train_id
JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
    UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) numbers ON numbers.n <= c.capacity
WHERE t.train_number IN ('2001', '3001', '5001');

INSERT INTO train_schedules (
    schedule_code, train_id, route_id, travel_date,
    departure_time, arrival_time, base_fare, status, created_at, updated_at
)
SELECT 'SCH-DEMO-CMB-GLE-02', t.id, r.id, CURDATE(),
       '07:10:00', '10:00:00', 950.00, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN routes r
WHERE t.train_number = '1001' AND r.route_code = 'RT-CMB-GLE'
  AND NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-CMB-GLE-02');

INSERT INTO train_schedules (
    schedule_code, train_id, route_id, travel_date,
    departure_time, arrival_time, base_fare, status, created_at, updated_at
)
SELECT 'SCH-DEMO-CMB-KDY-03', t.id, r.id, CURDATE(),
       '09:15:00', '12:00:00', 1350.00, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN routes r
WHERE t.train_number = '2001' AND r.route_code = 'RT-CMB-KDY'
  AND NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-CMB-KDY-03');

INSERT INTO train_schedules (
    schedule_code, train_id, route_id, travel_date,
    departure_time, arrival_time, base_fare, status, created_at, updated_at
)
SELECT 'SCH-DEMO-CMB-KDY-04', t.id, r.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY),
       '06:30:00', '09:15:00', 1800.00, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN routes r
WHERE t.train_number = '3001' AND r.route_code = 'RT-CMB-KDY'
  AND NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-CMB-KDY-04');

INSERT INTO train_schedules (
    schedule_code, train_id, route_id, travel_date,
    departure_time, arrival_time, base_fare, status, created_at, updated_at
)
SELECT 'SCH-DEMO-CMB-GLE-03', t.id, r.id, DATE_ADD(CURDATE(), INTERVAL 2 DAY),
       '08:20:00', '11:10:00', 1400.00, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN routes r
WHERE t.train_number = '5001' AND r.route_code = 'RT-CMB-GLE'
  AND NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-CMB-GLE-03');

INSERT INTO train_schedules (
    schedule_code, train_id, route_id, travel_date,
    departure_time, arrival_time, base_fare, status, created_at, updated_at
)
SELECT 'SCH-DEMO-KDY-CMB-03', t.id, r.id, DATE_ADD(CURDATE(), INTERVAL 3 DAY),
       '15:00:00', '17:45:00', 1200.00, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN routes r
WHERE t.train_number = '1005' AND r.route_code = 'RT-KDY-CMB'
  AND NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-KDY-CMB-03');
