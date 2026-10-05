package lk.sliit.railconnect.features.route.service;

import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.domain.RouteStatus;
import lk.sliit.railconnect.features.route.dto.RouteForm;
import lk.sliit.railconnect.features.route.repository.RouteRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class RouteService {
    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> list(String query) {
        if (query == null || query.isBlank()) {
            return routeRepository.findAllByOrderByRouteCode();
        }
        return routeRepository.findByStartStationContainingIgnoreCaseOrEndStationContainingIgnoreCaseOrderByStartStation(query, query);
    }

    @Transactional(readOnly = true)
    public List<Route> active() {
        return routeRepository.findByStatusOrderByStartStation(RouteStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<String> activeStationNames() {
        return active().stream()
                .flatMap(route -> Stream.of(route.getStartStation(), route.getEndStation()))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public Route require(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route was not found."));
    }

    @Transactional
    public Route create(RouteForm form) {
        validate(form, null);
        return routeRepository.save(new Route(form.getRouteCode().trim(), form.getStartStation().trim(),
                form.getEndStation().trim(), form.getDistanceKm()));
    }

    @Transactional
    public Route update(Long id, RouteForm form) {
        Route route = require(id);
        validate(form, id);
        route.update(form.getRouteCode().trim(), form.getStartStation().trim(), form.getEndStation().trim(),
                form.getDistanceKm(), form.getStatus());
        return route;
    }

    @Transactional
    public void toggleActive(Long id) {
        Route route = require(id);
        RouteStatus next = route.getStatus() == RouteStatus.INACTIVE ? RouteStatus.ACTIVE : RouteStatus.INACTIVE;
        route.update(route.getRouteCode(), route.getStartStation(), route.getEndStation(), route.getDistanceKm(), next);
    }

    private void validate(RouteForm form, Long id) {
        if (form.getStartStation().trim().equalsIgnoreCase(form.getEndStation().trim())) {
            throw new BusinessRuleException("Start and end stations must be different.");
        }
        boolean duplicate = id == null
                ? routeRepository.existsByRouteCodeIgnoreCase(form.getRouteCode().trim())
                : routeRepository.existsByRouteCodeIgnoreCaseAndIdNot(form.getRouteCode().trim(), id);
        if (duplicate) {
            throw new BusinessRuleException("Route code already exists.");
        }
    }
}
