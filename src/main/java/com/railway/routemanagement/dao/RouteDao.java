package com.railway.routemanagement.dao;

import com.railway.routemanagement.model.Route;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - DATA ACCESS OBJECT (DAO) INTERFACE
 * ============================================================================
 * 
 * Architectural Layer: Data Access / Persistence Tier (DAO Pattern)
 * 
 * Responsibility:
 * Defines pure data persistence and retrieval contracts.
 * Isolates SQL and persistence mechanisms from the business service layer.
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public interface RouteDao {

    /**
     * Persists a newly created Route entity into the database.
     *
     * @param route The Route entity to insert
     * @return The persisted Route
     */
    Route save(Route route);

    /**
     * Finds a route record by its primary key.
     *
     * @param routeId Unique route identifier
     * @return Optional containing the Route if found, otherwise empty Optional
     */
    Optional<Route> findById(String routeId);

    /**
     * Retrieves all route records (ACTIVE, INACTIVE, ARCHIVED) for administrative auditing.
     *
     * @return List of all routes
     */
    List<Route> findAll();

    /**
     * Retrieves only operational routes (status == 'ACTIVE').
     * Used by Module 3 (Train Schedules) for train allocation.
     *
     * @return List of active routes
     */
    List<Route> findAllActive();

    /**
     * Searches routes where origin or destination matches the query string.
     *
     * @param stationName Station search term
     * @return List of matching routes
     */
    List<Route> findByStation(String stationName);

    /**
     * Filters routes by specific operational status.
     *
     * @param status Route status
     * @return List of matching routes
     */
    List<Route> findByStatus(String status);

    /**
     * Updates an existing route's endpoints, distance, and status.
     *
     * @param route Route containing updated values
     * @return true if update affected 1 row, false otherwise
     */
    boolean update(Route route);

    /**
     * Performs a SOFT DELETE on the route by updating status to 'ARCHIVED'.
     * CRITICAL: Never executes SQL "DELETE FROM routes".
     *
     * @param routeId Unique route identifier
     * @return true if marked ARCHIVED, false if not found
     */
    boolean softDelete(String routeId);

    /**
     * Checks if a conflicting non-archived route already exists between two stations.
     *
     * @param startStation   Origin station
     * @param endStation     Destination station
     * @param excludeRouteId Optional route ID to ignore (for updates)
     * @return true if duplicate exists, false otherwise
     */
    boolean existsDuplicate(String startStation, String endStation, String excludeRouteId);
}
