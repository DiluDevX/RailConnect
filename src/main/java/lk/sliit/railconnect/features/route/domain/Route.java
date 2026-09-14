package lk.sliit.railconnect.features.route.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "routes")
public class Route extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String routeCode;

    @Column(nullable = false, length = 100)
    private String startStation;

    @Column(nullable = false, length = 100)
    private String endStation;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RouteStatus status = RouteStatus.ACTIVE;

    protected Route() {
    }

    public Route(String routeCode, String startStation, String endStation, BigDecimal distanceKm) {
        this.routeCode = routeCode;
        this.startStation = startStation;
        this.endStation = endStation;
        this.distanceKm = distanceKm;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public void update(String routeCode, String startStation, String endStation, BigDecimal distanceKm, RouteStatus status) {
        this.routeCode = routeCode;
        this.startStation = startStation;
        this.endStation = endStation;
        this.distanceKm = distanceKm;
        this.status = status;
    }
}
