package lk.sliit.railconnect.features.route.repository;

import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.domain.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByRouteCodeIgnoreCase(String routeCode);
    boolean existsByRouteCodeIgnoreCaseAndIdNot(String routeCode, Long id);
    List<Route> findAllByOrderByRouteCode();
    List<Route> findByStatusOrderByStartStation(RouteStatus status);
    List<Route> findByStartStationContainingIgnoreCaseOrEndStationContainingIgnoreCaseOrderByStartStation(String start, String end);
}
