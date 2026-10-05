package lk.sliit.railconnect.features.booking.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lk.sliit.railconnect.features.carriage.domain.Seat;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_reservations", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "seat_id"}))
public class SeatReservation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private TicketBooking booking;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @jakarta.persistence.Column(nullable = false, length = 20)
    private ReservationStatus status;

    @jakarta.persistence.Column(nullable = false)
    private LocalDateTime expiresAt;

    protected SeatReservation() {
    }

    public SeatReservation(TrainSchedule schedule, Seat seat, TicketBooking booking, LocalDateTime expiresAt) {
        this.schedule = schedule;
        this.seat = seat;
        this.booking = booking;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.HELD;
    }

    public TrainSchedule getSchedule() { return schedule; }
    public Seat getSeat() { return seat; }
    public TicketBooking getBooking() { return booking; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public boolean blocksBooking(LocalDateTime now) {
        return status == ReservationStatus.CONFIRMED || (status == ReservationStatus.HELD && expiresAt.isAfter(now));
    }

    public void holdFor(TicketBooking booking, LocalDateTime expiresAt) {
        this.booking = booking;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.HELD;
    }

    public void confirm() { this.status = ReservationStatus.CONFIRMED; }
    public void release() { this.status = ReservationStatus.RELEASED; }
    public void expire() { this.status = ReservationStatus.EXPIRED; }
}
