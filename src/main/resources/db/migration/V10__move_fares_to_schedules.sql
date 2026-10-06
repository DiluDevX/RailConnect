-- Preserve the old effective fare for each class before switching to schedule pricing.
ALTER TABLE train_schedules
    ADD COLUMN first_class_fare DECIMAL(10,2) NULL,
    ADD COLUMN second_class_fare DECIMAL(10,2) NULL,
    ADD COLUMN third_class_fare DECIMAL(10,2) NULL;

UPDATE train_schedules s JOIN trains t ON t.id = s.train_id
SET s.first_class_fare = ROUND(COALESCE(NULLIF(t.first_class_fare, 0), s.base_fare * 1.50), 2),
    s.second_class_fare = ROUND(COALESCE(NULLIF(t.second_class_fare, 0), s.base_fare), 2),
    s.third_class_fare = ROUND(COALESCE(NULLIF(t.third_class_fare, 0), s.base_fare * 0.75), 2);

ALTER TABLE train_schedules
    MODIFY COLUMN first_class_fare DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN second_class_fare DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN third_class_fare DECIMAL(10,2) NOT NULL,
    DROP CHECK chk_schedule_fare,
    DROP COLUMN base_fare,
    ADD CONSTRAINT chk_schedule_class_fares CHECK (
        first_class_fare >= 0 AND second_class_fare >= 0 AND third_class_fare >= 0
    );

-- Historical booking totals, per-seat fares and payment amounts remain untouched.
