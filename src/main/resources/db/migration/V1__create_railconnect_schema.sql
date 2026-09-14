CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE trains (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    train_number VARCHAR(30) NOT NULL UNIQUE,
    train_name VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE routes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_code VARCHAR(30) NOT NULL UNIQUE,
    start_station VARCHAR(100) NOT NULL,
    end_station VARCHAR(100) NOT NULL,
    distance_km DECIMAL(8,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT chk_route_distance CHECK (distance_km > 0),
    CONSTRAINT chk_route_stations CHECK (LOWER(start_station) <> LOWER(end_station))
);

CREATE TABLE train_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_code VARCHAR(40) NOT NULL UNIQUE,
    train_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    travel_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    base_fare DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_schedule_train FOREIGN KEY (train_id) REFERENCES trains(id),
    CONSTRAINT fk_schedule_route FOREIGN KEY (route_id) REFERENCES routes(id),
    CONSTRAINT chk_schedule_fare CHECK (base_fare >= 0),
    CONSTRAINT chk_schedule_times CHECK (departure_time < arrival_time)
);

CREATE INDEX idx_schedule_search ON train_schedules(travel_date, status, route_id);
CREATE INDEX idx_schedule_train_time ON train_schedules(train_id, travel_date, departure_time, arrival_time);

CREATE TABLE carriages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    train_id BIGINT NOT NULL,
    carriage_number VARCHAR(20) NOT NULL,
    class_type VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_carriage_train_number UNIQUE (train_id, carriage_number),
    CONSTRAINT fk_carriage_train FOREIGN KEY (train_id) REFERENCES trains(id),
    CONSTRAINT chk_carriage_capacity CHECK (capacity BETWEEN 4 AND 120)
);

CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carriage_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_seat_carriage_number UNIQUE (carriage_id, seat_number),
    CONSTRAINT fk_seat_carriage FOREIGN KEY (carriage_id) REFERENCES carriages(id)
);

CREATE TABLE ticket_bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    passenger_name VARCHAR(120) NOT NULL,
    contact_email VARCHAR(160) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    hold_expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_schedule FOREIGN KEY (schedule_id) REFERENCES train_schedules(id),
    CONSTRAINT chk_booking_amount CHECK (total_amount >= 0)
);

CREATE INDEX idx_booking_user ON ticket_bookings(user_id, created_at);
CREATE INDEX idx_booking_schedule ON ticket_bookings(schedule_id, status);

CREATE TABLE booking_seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    fare_amount DECIMAL(10,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_booking_seat UNIQUE (booking_id, seat_id),
    CONSTRAINT fk_booking_seat_booking FOREIGN KEY (booking_id) REFERENCES ticket_bookings(id),
    CONSTRAINT fk_booking_seat_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
);

CREATE TABLE seat_reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    booking_id BIGINT,
    status VARCHAR(20) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_reservation_schedule_seat UNIQUE (schedule_id, seat_id),
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (schedule_id) REFERENCES train_schedules(id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT fk_reservation_booking FOREIGN KEY (booking_id) REFERENCES ticket_bookings(id)
);

CREATE INDEX idx_reservation_booking ON seat_reservations(booking_id);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    attempt_number INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    method VARCHAR(30) NOT NULL,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_payment_attempt UNIQUE (booking_id, attempt_number),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES ticket_bookings(id),
    CONSTRAINT chk_payment_amount CHECK (amount >= 0)
);

CREATE TABLE complaints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complaint_reference VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    complaint_type VARCHAR(30) NOT NULL,
    subject VARCHAR(160) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    admin_response VARCHAR(2000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_complaint_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_complaint_booking FOREIGN KEY (booking_id) REFERENCES ticket_bookings(id)
);

CREATE INDEX idx_complaint_user ON complaints(user_id, created_at);
CREATE INDEX idx_complaint_status ON complaints(status, created_at);
