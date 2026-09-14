package com.railway.routemanagement.service;

import com.railway.routemanagement.dao.RouteDao;
import com.railway.routemanagement.dao.RouteDaoJdbcImpl;
import com.railway.routemanagement.model.Route;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - SERVICE TIER IMPLEMENTATION
 * ============================================================================
 * 
 * Architectural Layer: Business Logic Tier (3-Tier Architecture)
 * 
 * Responsibilities:
 * 1. Business Validation (stations non-empty, start != end, distance > 0).
 * 2. Uniqueness verification via RouteDao.
 * 3. Soft deletion enforcement (Archive status to protect Module 3 schedules).
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public class RouteServiceImpl implements RouteService {

    // DAO dependency injected (Dependency Inversion Principle)
    private final RouteDao routeDao;

    /**
     * Default constructor initializes JDBC DAO implementation.
     */
    public RouteServiceImpl() {
        this(new RouteDaoJdbcImpl());
    }

    /**
     * Constructor injection for unit testing / mocking.
     */
    public RouteServiceImpl(RouteDao routeDao) {
        this.routeDao = routeDao;
    }

    // ==========================================
    // 1. CREATE OPERATION
    // ==========================================
    @Override
    public Route createRoute(String startStation, String endStation, double distanceKm) {
        validateRouteParameters(startStation, endStation, distanceKm);

        if (routeDao.existsDuplicate(startStation, endStation, null)) {
            throw new IllegalStateException(String.format(
                "Duplicate Route Error: An active route between '%s' and '%s' already exists in the database.",
                startStation.trim(), endStation.trim()
            ));
        }

        // Generate enterprise route ID (e.g., RT-8A4B2C) or sequential
        String generatedId = "RT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Route newRoute = new Route(
            generatedId,
            startStation.trim(),
            endStation.trim(),
            distanceKm,
            Route.STATUS_ACTIVE
        );

        return routeDao.save(newRoute);
    }

    // ==========================================
    // 2. READ OPERATIONS
    // ==========================================
    @Override
    public Optional<Route> getRouteById(String routeId) {
        if (routeId == null || routeId.trim().isEmpty()) {
            return Optional.empty();
        }
        return routeDao.findById(routeId.trim());
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeDao.findAll();
    }

    @Override
    public List<Route> getActiveRoutes() {
        // Module 3 uses this method to ensure train schedules only attach to active tracks
        return routeDao.findAllActive();
    }

    @Override
    public List<Route> searchByStation(String stationName) {
        if (stationName == null || stationName.trim().isEmpty()) {
            return getAllRoutes();
        }
        return routeDao.findByStation(stationName.trim());
    }

    @Override
    public List<Route> filterByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            return getAllRoutes();
        }
        return routeDao.findByStatus(status.trim());
    }

    // ==========================================
    // 3. UPDATE OPERATION (UC STEP 3b)
    // ==========================================
    @Override
    public Route updateRoute(String routeId, String startStation, String endStation, double distanceKm, String status) {
        // Step 1: Ensure target route exists
        Route existing = routeDao.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot update: Route ID not found -> " + routeId));

        // Step 2: Validate business parameters
        validateRouteParameters(startStation, endStation, distanceKm);
        validateStatus(status);

        // Step 3: Check for duplicate conflict with OTHER existing routes
        if (routeDao.existsDuplicate(startStation, endStation, routeId)) {
            throw new IllegalStateException(String.format(
                "Conflict Error: Another route already connects '%s' and '%s'.",
                startStation.trim(), endStation.trim()
            ));
        }

        // Step 4: Apply updates
        existing.setStartStation(startStation.trim());
        existing.setEndStation(endStation.trim());
        existing.setDistanceKm(distanceKm);
        existing.setStatus(status.trim().toUpperCase());

        boolean updated = routeDao.update(existing);
        if (!updated) {
            throw new RuntimeException("Database update operation failed for Route ID: " + routeId);
        }

        return existing;
    }

    // ==========================================
    // 4. DELETE OPERATION (SOFT DELETE / ARCHIVE)
    // ==========================================
    @Override
    public boolean softDeleteRoute(String routeId) {
        /*
         * ENTERPRISE ARCHITECTURAL REQUIREMENT:
         * Never delete records physically from the database.
         * Soft deleting preserves foreign key data lineage for:
         * - Module 3: Historical train schedules
         * - Module 5: Passenger ticket transactions
         */
        if (routeId == null || routeId.trim().isEmpty()) {
            return false;
        }
        return routeDao.softDelete(routeId.trim());
    }

    // ==========================================
    // PRIVATE VALIDATION HELPERS
    // ==========================================
    private void validateRouteParameters(String startStation, String endStation, double distanceKm) {
        if (startStation == null || startStation.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Origin station cannot be empty.");
        }
        if (endStation == null || endStation.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Destination station cannot be empty.");
        }
        if (startStation.trim().equalsIgnoreCase(endStation.trim())) {
            throw new IllegalArgumentException(String.format(
                "Validation Error: Origin and Destination stations cannot be identical ('%s').",
                startStation.trim()
            ));
        }
        if (distanceKm <= 0.0) {
            throw new IllegalArgumentException(String.format(
                "Validation Error: Track distance must be strictly greater than 0 km. Provided: %.2f km.",
                distanceKm
            ));
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Validation Error: Status cannot be empty.");
        }
        String clean = status.trim().toUpperCase();
        if (!clean.equals(Route.STATUS_ACTIVE) && !clean.equals(Route.STATUS_INACTIVE) && !clean.equals(Route.STATUS_ARCHIVED)) {
            throw new IllegalArgumentException("Validation Error: Invalid status '" + status + "'. Allowed: ACTIVE, INACTIVE, ARCHIVED.");
        }
    }
}
