package lk.sliit.railconnect.features.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "train_schedules")
public class TrainSchedule extends BaseEntity {
    @Column(nullable = false, unique = true, length = 40)
    private String scheduleCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    protected TrainSchedule() {
    }

    public TrainSchedule(String scheduleCode, Train train, Route route, LocalDate travelDate,
                         LocalTime departureTime, LocalTime arrivalTime, BigDecimal baseFare) {
        this.scheduleCode = scheduleCode;
        this.train = train;
        this.route = route;
        this.travelDate = travelDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.baseFare = baseFare;
    }

    public String getScheduleCode() { return scheduleCode; }
    public Train getTrain() { return train; }
    public Route getRoute() { return route; }
    public LocalDate getTravelDate() { return travelDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public BigDecimal getBaseFare() { return baseFare; }
    public ScheduleStatus getStatus() { return status; }

    public void update(String scheduleCode, Train train, Route route, LocalDate travelDate,
                       LocalTime departureTime, LocalTime arrivalTime, BigDecimal baseFare, ScheduleStatus status) {
        this.scheduleCode = scheduleCode;
        this.train = train;
        this.route = route;
        this.travelDate = travelDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.baseFare = baseFare;
        this.status = status;
    }

    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }
}
