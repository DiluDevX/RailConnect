package com.railway.routemanagement.service;

import com.railway.routemanagement.model.Route;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - SERVICE TIER CONTRACT
 * ============================================================================
 * 
 * Architectural Layer: Business Logic / Service Tier
 * Encapsulates domain business rules and transactions.
 * Decoupled from Controller (Presentation) and DAO (Persistence).
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public interface RouteService {

    Route createRoute(String startStation, String endStation, double distanceKm);

    Optional<Route> getRouteById(String routeId);

    List<Route> getAllRoutes();

    List<Route> getActiveRoutes();

    List<Route> searchByStation(String stationName);

    List<Route> filterByStatus(String status);

    Route updateRoute(String routeId, String startStation, String endStation, double distanceKm, String status);

    boolean softDeleteRoute(String routeId);
}
