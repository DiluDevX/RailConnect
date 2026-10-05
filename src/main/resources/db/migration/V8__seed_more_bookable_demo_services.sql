-- Demo services only: no users, bookings, payments or complaints are inserted.
-- Dates are relative to the first application of this migration, not every restart.
INSERT INTO trains (train_number, train_name, description, status,
                    first_class_fare, second_class_fare, third_class_fare, created_at, updated_at)
SELECT demo.number, demo.name, 'Additional presentation demo service', 'ACTIVE',
       1800.00, 1200.00, 800.00, NOW(), NOW()
FROM (
    SELECT 'DEMO-K01' AS number, 'Kandy Morning Demo' AS name
    UNION ALL SELECT 'DEMO-K02', 'Kandy Midday Demo'
    UNION ALL SELECT 'DEMO-K03', 'Kandy Afternoon Demo'
    UNION ALL SELECT 'DEMO-G01', 'Galle Morning Demo'
    UNION ALL SELECT 'DEMO-G02', 'Galle Midday Demo'
    UNION ALL SELECT 'DEMO-G03', 'Galle Afternoon Demo'
    UNION ALL SELECT 'DEMO-C01', 'Colombo Morning Demo'
    UNION ALL SELECT 'DEMO-C02', 'Colombo Midday Demo'
    UNION ALL SELECT 'DEMO-C03', 'Colombo Afternoon Demo'
) demo
WHERE NOT EXISTS (SELECT 1 FROM trains t WHERE t.train_number = demo.number);

-- Each new train has 60 bookable seats, across all three classes.
INSERT INTO carriages (train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
SELECT t.id, classes.number, classes.type, 20, 'ACTIVE', NOW(), NOW()
FROM trains t CROSS JOIN (
    SELECT 'A01' AS number, 'FIRST' AS type
    UNION ALL SELECT 'B01', 'SECOND'
    UNION ALL SELECT 'C01', 'THIRD'
) classes
WHERE t.train_number IN ('DEMO-K01', 'DEMO-K02', 'DEMO-K03',
                        'DEMO-G01', 'DEMO-G02', 'DEMO-G03',
                        'DEMO-C01', 'DEMO-C02', 'DEMO-C03')
  AND NOT EXISTS (SELECT 1 FROM carriages c WHERE c.train_id = t.id AND c.carriage_number = classes.number);

INSERT INTO seats (carriage_id, seat_number, seat_type, status, created_at, updated_at)
SELECT c.id, LPAD(numbers.n, 2, '0'),
       CASE WHEN MOD(numbers.n, 4) IN (0, 1) THEN 'WINDOW' ELSE 'AISLE' END,
       'ACTIVE', NOW(), NOW()
FROM carriages c JOIN trains t ON t.id = c.train_id
CROSS JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
    UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
) numbers
WHERE t.train_number IN ('DEMO-K01', 'DEMO-K02', 'DEMO-K03',
                        'DEMO-G01', 'DEMO-G02', 'DEMO-G03',
                        'DEMO-C01', 'DEMO-C02', 'DEMO-C03')
  AND numbers.n <= c.capacity
  AND NOT EXISTS (SELECT 1 FROM seats s WHERE s.carriage_id = c.id AND s.seat_number = LPAD(numbers.n, 2, '0'));

-- One journey per new train per day: no overlapping services for the same train.
-- Three departures per route daily for the next 30 days, starting tomorrow.
INSERT INTO train_schedules (schedule_code, train_id, route_id, travel_date,
                            departure_time, arrival_time, base_fare, status, created_at, updated_at)
SELECT CONCAT('SCH-', t.train_number, '-', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL days.n DAY), '%Y%m%d')),
       t.id, r.id, DATE_ADD(CURDATE(), INTERVAL days.n DAY),
       CASE RIGHT(t.train_number, 1) WHEN '1' THEN '06:00:00' WHEN '2' THEN '10:00:00' ELSE '14:00:00' END,
       CASE RIGHT(t.train_number, 1) WHEN '1' THEN '09:00:00' WHEN '2' THEN '13:00:00' ELSE '17:00:00' END,
       1200.00, 'ACTIVE', NOW(), NOW()
FROM trains t JOIN routes r ON
    (t.train_number IN ('DEMO-K01', 'DEMO-K02', 'DEMO-K03') AND r.route_code = 'RT-CMB-KDY')
    OR (t.train_number IN ('DEMO-G01', 'DEMO-G02', 'DEMO-G03') AND r.route_code = 'RT-CMB-GLE')
    OR (t.train_number IN ('DEMO-C01', 'DEMO-C02', 'DEMO-C03') AND r.route_code = 'RT-KDY-CMB')
CROSS JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16
    UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
    UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28
    UNION ALL SELECT 29 UNION ALL SELECT 30
) days
WHERE t.status = 'ACTIVE' AND r.status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1 FROM train_schedules s
    WHERE s.schedule_code = CONCAT('SCH-', t.train_number, '-', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL days.n DAY), '%Y%m%d'))
  );
