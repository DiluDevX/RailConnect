package com.railway.routemanagement.model;

import java.util.Objects;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - CORE DOMAIN MODEL
 * ============================================================================
 * 
 * Architectural Layer: Model / Entity Tier
 * Represents the physical track connection between two railway stations.
 * 
 * Strict Boundary:
 * Zero scheduling times, trains, or ticket booking references.
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public class Route {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    private String routeId;
    private String startStation;
    private String endStation;
    private double distanceKm;
    private String status;

    public Route() {
    }

    public Route(String routeId, String startStation, String endStation, double distanceKm, String status) {
        this.routeId = routeId;
        this.startStation = startStation;
        this.endStation = endStation;
        this.distanceKm = distanceKm;
        this.status = status;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(routeId, route.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }

    @Override
    public String toString() {
        return String.format(
            "Route [ID: %-8s | %-16s -> %-16s | Distance: %6.1f km | Status: %-8s]",
            routeId, startStation, endStation, distanceKm, status
        );
    }
}
