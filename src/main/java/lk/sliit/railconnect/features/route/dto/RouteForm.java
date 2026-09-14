package lk.sliit.railconnect.features.route.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.domain.RouteStatus;

import java.math.BigDecimal;

public class RouteForm {
    @NotBlank
    @Size(max = 30)
    private String routeCode;

    @NotBlank
    @Size(max = 100)
    private String startStation;

    @NotBlank
    @Size(max = 100)
    private String endStation;

    @NotNull
    @DecimalMin(value = "0.1")
    private BigDecimal distanceKm;

    private RouteStatus status = RouteStatus.ACTIVE;

    public static RouteForm from(Route route) {
        RouteForm form = new RouteForm();
        form.routeCode = route.getRouteCode();
        form.startStation = route.getStartStation();
        form.endStation = route.getEndStation();
        form.distanceKm = route.getDistanceKm();
        form.status = route.getStatus();
        return form;
    }

    public String getRouteCode() { return routeCode; }
    public void setRouteCode(String routeCode) { this.routeCode = routeCode; }
    public String getStartStation() { return startStation; }
    public void setStartStation(String startStation) { this.startStation = startStation; }
    public String getEndStation() { return endStation; }
    public void setEndStation(String endStation) { this.endStation = endStation; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }
}
