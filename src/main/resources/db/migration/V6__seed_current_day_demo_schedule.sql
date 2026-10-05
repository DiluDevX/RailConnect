INSERT INTO train_schedules (id, schedule_code, train_id, route_id, travel_date, departure_time, arrival_time, base_fare, status, created_at, updated_at)
SELECT 4, 'SCH-DEMO-TODAY', 2, 1, '2026-10-05', '17:30:00', '20:15:00', 1200.00, 'ACTIVE', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM train_schedules WHERE schedule_code = 'SCH-DEMO-TODAY');
