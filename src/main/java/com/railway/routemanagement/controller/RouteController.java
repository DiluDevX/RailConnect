package com.railway.routemanagement.controller;

import com.railway.routemanagement.model.Route;
import com.railway.routemanagement.service.RouteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Spring Boot Annotations (Standard SE2030 Curriculum):
 * import org.springframework.http.HttpStatus;
 * import org.springframework.http.ResponseEntity;
 * import org.springframework.web.bind.annotation.*;
 */

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - PRESENTATION / CONTROLLER TIER
 * ============================================================================
 * 
 * Architectural Layer: Web / Controller Tier (3-Tier Architecture)
 * 
 * Endpoints:
 * - GET    /api/routes               -> List / search routes
 * - GET    /api/routes/{id}          -> Single route lookup
 * - POST   /api/routes               -> Create new route (UC Step 1)
 * - PUT    /api/routes/{id}          -> Update route details (UC Step 3b)
 * - POST   /api/routes/{id}/archive  -> Soft delete / archive route (UC Step 4)
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
// @RestController
// @RequestMapping("/api/routes")
// @CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    // Constructor Injection (SOLID DIP)
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * DTO for Create Route Request
     */
    public static class CreateRouteRequest {
        private String startStation;
        private String endStation;
        private double distanceKm;

        public String getStartStation() { return startStation; }
        public void setStartStation(String startStation) { this.startStation = startStation; }

        public String getEndStation() { return endStation; }
        public void setEndStation(String endStation) { this.endStation = endStation; }

        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    }

    /**
     * DTO for Update Route Request (Use Case Step 3b)
     */
    public static class UpdateRouteRequest {
        private String startStation;
        private String endStation;
        private double distanceKm;
        private String status;

        public String getStartStation() { return startStation; }
        public void setStartStation(String startStation) { this.startStation = startStation; }

        public String getEndStation() { return endStation; }
        public void setEndStation(String endStation) { this.endStation = endStation; }

        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // @GetMapping
    public List<Route> getRoutes(String station, String status) {
        if (station != null && !station.trim().isEmpty()) {
            return routeService.searchByStation(station);
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return routeService.filterByStatus(status);
        }
        return routeService.getAllRoutes();
    }

    // @GetMapping("/{id}")
    public Route getRouteById(String id) {
        return routeService.getRouteById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route ID " + id + " not found."));
    }

    // @PostMapping
    // @ResponseStatus(HttpStatus.CREATED)
    public Route createRoute(CreateRouteRequest request) {
        return routeService.createRoute(
            request.getStartStation(),
            request.getEndStation(),
            request.getDistanceKm()
        );
    }

    /**
     * UC Step 3b: Update Route Details
     */
    // @PutMapping("/{id}")
    public Route updateRoute(String id, UpdateRouteRequest request) {
        return routeService.updateRoute(
            id,
            request.getStartStation(),
            request.getEndStation(),
            request.getDistanceKm(),
            request.getStatus()
        );
    }

    /**
     * UC Step 4: Soft Delete (Archive)
     */
    // @PostMapping("/{id}/archive")
    public Map<String, Object> archiveRoute(String id) {
        boolean success = routeService.softDeleteRoute(id);
        Map<String, Object> response = new HashMap<>();
        if (!success) {
            throw new IllegalArgumentException("Route ID " + id + " not found for archiving.");
        }
        response.put("success", true);
        response.put("message", "Route " + id + " archived. Historical schedules preserved.");
        response.put("routeId", id);
        return response;
    }
}
