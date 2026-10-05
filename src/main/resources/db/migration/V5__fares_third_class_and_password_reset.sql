ALTER TABLE trains ADD COLUMN first_class_fare DECIMAL(10,2) NULL;
ALTER TABLE trains ADD COLUMN second_class_fare DECIMAL(10,2) NULL;
ALTER TABLE trains ADD COLUMN third_class_fare DECIMAL(10,2) NULL;

UPDATE trains SET first_class_fare = 1800.00, second_class_fare = 1200.00, third_class_fare = 900.00 WHERE id = 1;
UPDATE trains SET first_class_fare = 1800.00, second_class_fare = 1200.00, third_class_fare = 900.00 WHERE id = 2;
UPDATE trains SET first_class_fare = 1875.00, second_class_fare = 1250.00, third_class_fare = 937.50 WHERE id = 3;

INSERT INTO carriages (id, train_id, carriage_number, class_type, capacity, status, created_at, updated_at)
VALUES (4, 2, 'C01', 'THIRD', 12, 'ACTIVE', NOW(), NOW());

INSERT INTO seats (id, carriage_id, seat_number, seat_type, status, created_at, updated_at) VALUES
(33, 4, '01', 'WINDOW', 'ACTIVE', NOW(), NOW()), (34, 4, '02', 'AISLE', 'ACTIVE', NOW(), NOW()),
(35, 4, '03', 'AISLE', 'ACTIVE', NOW(), NOW()), (36, 4, '04', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(37, 4, '05', 'WINDOW', 'ACTIVE', NOW(), NOW()), (38, 4, '06', 'AISLE', 'ACTIVE', NOW(), NOW()),
(39, 4, '07', 'AISLE', 'ACTIVE', NOW(), NOW()), (40, 4, '08', 'WINDOW', 'ACTIVE', NOW(), NOW()),
(41, 4, '09', 'WINDOW', 'ACTIVE', NOW(), NOW()), (42, 4, '10', 'AISLE', 'ACTIVE', NOW(), NOW()),
(43, 4, '11', 'AISLE', 'ACTIVE', NOW(), NOW()), (44, 4, '12', 'WINDOW', 'ACTIVE', NOW(), NOW());

CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(80) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);
