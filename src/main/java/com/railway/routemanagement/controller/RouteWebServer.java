package com.railway.routemanagement.controller;

import com.railway.routemanagement.dao.RouteDaoJdbcImpl;
import com.railway.routemanagement.model.Route;
import com.railway.routemanagement.service.RouteService;
import com.railway.routemanagement.service.RouteServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================================
 * MODULE 2: ROUTE MANAGEMENT (UC-05) - ENTERPRISE WEB SERVER & DISPATCHER
 * ============================================================================
 * 
 * Architectural Role:
 * Embedded HTTP server powering both the Frontend and the REST API.
 * Dispatches requests down through the 3-Tier Layer:
 * RouteWebServer (Controller) -> RouteServiceImpl (Service) -> RouteDaoJdbcImpl (DAO) -> MySQL
 * 
 * @author Railway Management System Architecture Team (SE2030)
 */
public class RouteWebServer {

    private static final int PORT = 8080;
    private final RouteService routeService;

    public RouteWebServer() {
        // Assembles 3-Tier stack: Service with JDBC DAO
        this.routeService = new RouteServiceImpl(new RouteDaoJdbcImpl());
    }

    public static void main(String[] args) throws IOException {
        RouteWebServer server = new RouteWebServer();
        server.start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/routes", new RoutesApiHandler());
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("================================================================================");
        System.out.println("  MODULE 2: ROUTE MANAGEMENT (UC-05) - 3-TIER ENTERPRISE SERVER READY");
        System.out.println("================================================================================");
        System.out.println("  -> UI Dashboard:   http://localhost:" + PORT);
        System.out.println("  -> REST Endpoint:  http://localhost:" + PORT + "/api/routes");
        System.out.println("  -> Architecture:   Controller -> Service -> JDBC DAO -> MySQL");
        System.out.println("================================================================================\n");
    }

    class RoutesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                // 1. Soft Delete: POST /api/routes/{id}/archive
                if (path.matches("^/api/routes/[^/]+/archive/?$") && method.equals("POST")) {
                    handleArchiveRoute(exchange, path);
                    return;
                }

                // 2. Update Route (UC Step 3b): PUT /api/routes/{id}
                if (path.matches("^/api/routes/[^/]+/?$") && method.equals("PUT")) {
                    handleUpdateRoute(exchange, path);
                    return;
                }

                // 3. Single Route lookup: GET /api/routes/{id}
                if (path.matches("^/api/routes/[^/]+/?$") && method.equals("GET")) {
                    handleGetRouteById(exchange, path);
                    return;
                }

                // 4. List all / search: GET /api/routes
                if (method.equals("GET")) {
                    handleGetRoutes(exchange);
                    return;
                }

                // 5. Create Route: POST /api/routes
                if (method.equals("POST")) {
                    handleCreateRoute(exchange);
                    return;
                }

                sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            } catch (Exception ex) {
                sendJsonResponse(exchange, 500, "{\"error\": \"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }

        private void handleGetRoutes(HttpExchange exchange) throws IOException {
            List<Route> routes = routeService.getAllRoutes();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < routes.size(); i++) {
                json.append(routeToJson(routes.get(i)));
                if (i < routes.size() - 1) json.append(",");
            }
            json.append("]");
            sendJsonResponse(exchange, 200, json.toString());
        }

        private void handleGetRouteById(HttpExchange exchange, String path) throws IOException {
            String routeId = extractIdFromPath(path, "^/api/routes/([^/]+)/?$");
            Optional<Route> routeOpt = routeService.getRouteById(routeId);

            if (routeOpt.isPresent()) {
                sendJsonResponse(exchange, 200, routeToJson(routeOpt.get()));
            } else {
                sendJsonResponse(exchange, 404, "{\"error\": \"Route not found: " + routeId + "\"}");
            }
        }

        private void handleCreateRoute(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            String startStation = extractJsonField(body, "startStation");
            String endStation = extractJsonField(body, "endStation");
            String distanceStr = extractJsonField(body, "distanceKm");

            if (startStation == null || endStation == null || distanceStr == null) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Missing required fields: startStation, endStation, distanceKm\"}");
                return;
            }

            double distanceKm;
            try {
                distanceKm = Double.parseDouble(distanceStr);
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Invalid distance format.\"}");
                return;
            }

            try {
                Route created = routeService.createRoute(startStation, endStation, distanceKm);
                sendJsonResponse(exchange, 201, routeToJson(created));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                sendJsonResponse(exchange, 400, "{\"error\": \"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }

        /**
         * UC Step 3b: Update Route Details
         */
        private void handleUpdateRoute(HttpExchange exchange, String path) throws IOException {
            String routeId = extractIdFromPath(path, "^/api/routes/([^/]+)/?$");
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            String startStation = extractJsonField(body, "startStation");
            String endStation = extractJsonField(body, "endStation");
            String distanceStr = extractJsonField(body, "distanceKm");
            String status = extractJsonField(body, "status");

            if (startStation == null || endStation == null || distanceStr == null || status == null) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Missing fields for update. Required: startStation, endStation, distanceKm, status\"}");
                return;
            }

            double distanceKm;
            try {
                distanceKm = Double.parseDouble(distanceStr);
            } catch (NumberFormatException e) {
                sendJsonResponse(exchange, 400, "{\"error\": \"Invalid distance number format.\"}");
                return;
            }

            try {
                Route updated = routeService.updateRoute(routeId, startStation, endStation, distanceKm, status);
                sendJsonResponse(exchange, 200, routeToJson(updated));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                sendJsonResponse(exchange, 400, "{\"error\": \"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }

        private void handleArchiveRoute(HttpExchange exchange, String path) throws IOException {
            String routeId = extractIdFromPath(path, "^/api/routes/([^/]+)/archive/?$");
            boolean archived = routeService.softDeleteRoute(routeId);

            if (archived) {
                sendJsonResponse(exchange, 200, String.format(
                    "{\"success\": true, \"message\": \"Route %s soft-deleted (status = ARCHIVED). Historical schedules preserved.\", \"routeId\": \"%s\"}",
                    routeId, routeId
                ));
            } else {
                sendJsonResponse(exchange, 404, String.format("{\"error\": \"Route ID '%s' not found for archiving.\"}", routeId));
            }
        }

        private String extractIdFromPath(String path, String regex) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return "";
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Path htmlPath = Paths.get("src/main/resources/static/index.html");
            if (!Files.exists(htmlPath)) {
                htmlPath = Paths.get("/Users/lakiminayasiru/.gemini/antigravity/scratch/train-route-management/src/main/resources/static/index.html");
            }

            if (Files.exists(htmlPath)) {
                byte[] bytes = Files.readAllBytes(htmlPath);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } else {
                String fallback = "<html><body><h2>Error: index.html not found.</h2></body></html>";
                exchange.sendResponseHeaders(404, fallback.length());
                try (OutputStream os = exchange.getResponseBody()) { os.write(fallback.getBytes(StandardCharsets.UTF_8)); }
            }
        }
    }

    private static String routeToJson(Route r) {
        return String.format(
            "{\"routeId\":\"%s\",\"startStation\":\"%s\",\"endStation\":\"%s\",\"distanceKm\":%.2f,\"status\":\"%s\"}",
            escapeJson(r.getRouteId()), escapeJson(r.getStartStation()),
            escapeJson(r.getEndStation()), r.getDistanceKm(), escapeJson(r.getStatus())
        );
    }

    private static String extractJsonField(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*(?:\"([^\"]*)\"|([0-9.]+))");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1) != null ? m.group(1) : m.group(2);
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
}
