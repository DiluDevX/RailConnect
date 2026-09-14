package lk.sliit.railconnect.features.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lk.sliit.railconnect.features.carriage.domain.Seat;
import lk.sliit.railconnect.shared.domain.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats", uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "seat_id"}))
public class BookingSeat extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private TicketBooking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fareAmount;

    protected BookingSeat() {
    }

    public BookingSeat(TicketBooking booking, Seat seat, BigDecimal fareAmount) {
        this.booking = booking;
        this.seat = seat;
        this.fareAmount = fareAmount;
    }

    public TicketBooking getBooking() { return booking; }
    public Seat getSeat() { return seat; }
    public BigDecimal getFareAmount() { return fareAmount; }
}
