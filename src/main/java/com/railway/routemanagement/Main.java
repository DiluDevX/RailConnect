package com.railway.routemanagement;

import com.railway.routemanagement.dao.RouteDao;
import com.railway.routemanagement.dao.RouteDaoJdbcImpl;
import com.railway.routemanagement.model.Route;
import com.railway.routemanagement.service.RouteService;
import com.railway.routemanagement.service.RouteServiceImpl;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - 3-TIER TEST RUNNER
 * ============================================================================
 * 
 * Verifies end-to-end integration across the 3 Tiers:
 * Client -> Service Tier -> DAO Persistence Tier -> MySQL Database
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  MODULE 2: ROUTE MANAGEMENT (UC-05) - 3-TIER ARCHITECTURE VERIFICATION");
        System.out.println("================================================================================");
        System.out.println("Tier 1: Web / Controller (RouteController / RouteWebServer)");
        System.out.println("Tier 2: Business Logic (RouteServiceImpl)");
        System.out.println("Tier 3: Persistence (RouteDaoJdbcImpl -> MySQL)\n");

        // Dependency Injection across layers
        RouteDao routeDao = new RouteDaoJdbcImpl();
        RouteService routeService = new RouteServiceImpl(routeDao);

        // 1. CREATE
        System.out.println(">> 1. Creating Routes through Service into Persistence Tier:");
        Route r1 = routeService.createRoute("Colombo Fort", "Kandy", 116.0);
        Route r2 = routeService.createRoute("Colombo Fort", "Galle", 119.5);
        System.out.println("   Saved: " + r1);
        System.out.println("   Saved: " + r2);

        // 2. READ & SEARCH
        System.out.println("\n>> 2. Querying Active Tracks from DAO:");
        List<Route> active = routeService.getActiveRoutes();
        active.forEach(r -> System.out.println("   " + r));

        // 3. UPDATE (UC Step 3b)
        System.out.println("\n>> 3. Updating Route RT-1002 (UC Step 3b):");
        Route updated = routeService.updateRoute(r2.getRouteId(), "Colombo Fort", "Galle", 121.0, Route.STATUS_ACTIVE);
        System.out.println("   Updated: " + updated);

        // 4. SOFT DELETE (UC Step 4)
        System.out.println("\n>> 4. Soft-Deleting / Archiving Route (UC Step 4):");
        boolean archived = routeService.softDeleteRoute(r2.getRouteId());
        System.out.println("   Archive operation success: " + archived);

        Optional<Route> check = routeService.getRouteById(r2.getRouteId());
        System.out.println("   Integrity Check (Record Still Exists): " + check.orElse(null));

        System.out.println("\n================================================================================");
        System.out.println("  VERIFICATION COMPLETE - 3-TIER SEPARATION READY FOR EVALUATION");
        System.out.println("================================================================================");
    }
}
