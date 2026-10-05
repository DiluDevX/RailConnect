CREATE TABLE maintenance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    maintenance_type VARCHAR(80) NOT NULL,
    train_id BIGINT NOT NULL,
    maintenance_date DATE NOT NULL,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_maintenance_train FOREIGN KEY (train_id) REFERENCES trains (id)
);

INSERT INTO maintenance (maintenance_type, train_id, maintenance_date, description, status, created_at, updated_at)
SELECT 'Bogie inspection', t.id, '2026-10-05', 'In-progress inspection while the train is marked for maintenance.', 'IN_PROGRESS', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'
FROM trains t
WHERE t.train_number = '4001'
  AND NOT EXISTS (
      SELECT 1 FROM maintenance m WHERE m.train_id = t.id AND m.maintenance_type = 'Bogie inspection'
  );

INSERT INTO maintenance (maintenance_type, train_id, maintenance_date, description, status, created_at, updated_at)
SELECT 'Engine service', t.id, '2026-10-08', 'Scheduled routine engine inspection.', 'SCHEDULED', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'
FROM trains t
WHERE t.train_number = '1001'
  AND NOT EXISTS (
      SELECT 1 FROM maintenance m WHERE m.train_id = t.id AND m.maintenance_type = 'Engine service'
  );

INSERT INTO maintenance (maintenance_type, train_id, maintenance_date, description, status, created_at, updated_at)
SELECT 'Brake check', t.id, '2026-09-25', 'Completed brake inspection kept for history.', 'COMPLETED', '2026-10-05 00:00:00.000', '2026-10-05 00:00:00.000'
FROM trains t
WHERE t.train_number = '1005'
  AND NOT EXISTS (
      SELECT 1 FROM maintenance m WHERE m.train_id = t.id AND m.maintenance_type = 'Brake check'
  );
