-- ============================================================================
-- MODULE 2: ROUTE MANAGEMENT (UC-05) - DATABASE SCHEMA (MySQL)
-- ============================================================================
-- Purpose:
-- Establishes the relational persistence layer for the railway track definitions.
-- Adheres to 3rd Normal Form (3NF) and preserves historical integrity for
-- Module 3 (Train Schedule Management) foreign key references.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS railway_db;
USE railway_db;

-- 1. Routes Table
CREATE TABLE IF NOT EXISTS routes (
    route_id VARCHAR(20) NOT NULL,
    start_station VARCHAR(100) NOT NULL,
    end_station VARCHAR(100) NOT NULL,
    distance_km DECIMAL(7, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (route_id),
    
    -- Business Domain Database Constraints
    CONSTRAINT chk_positive_distance CHECK (distance_km > 0),
    CONSTRAINT chk_different_stations CHECK (start_station <> end_station),
    CONSTRAINT chk_valid_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Performance Indexes (Optimizes Search and Schedule queries)
CREATE INDEX IF NOT EXISTS idx_routes_status ON routes(status);
CREATE INDEX IF NOT EXISTS idx_routes_start_station ON routes(start_station);
CREATE INDEX IF NOT EXISTS idx_routes_end_station ON routes(end_station);

-- 3. Seed / Sample Data (Sri Lankan Railway Tracks)
INSERT INTO routes (route_id, start_station, end_station, distance_km, status)
VALUES 
    ('RT-1001', 'Colombo Fort', 'Kandy', 116.00, 'ACTIVE'),
    ('RT-1002', 'Colombo Fort', 'Galle', 119.50, 'ACTIVE'),
    ('RT-1003', 'Kandy', 'Badulla', 138.50, 'ACTIVE'),
    ('RT-1004', 'Colombo Fort', 'Jaffna', 398.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    start_station = VALUES(start_station),
    end_station = VALUES(end_station),
    distance_km = VALUES(distance_km);
