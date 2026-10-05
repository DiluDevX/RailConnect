package lk.sliit.railconnect.features.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_bookings")
public class TicketBooking extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TrainSchedule schedule;

    @Column(nullable = false, length = 120)
    private String passengerName;

    @Column(nullable = false, length = 160)
    private String contactEmail;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT;

    @Column(nullable = false)
    private LocalDateTime holdExpiresAt;

    protected TicketBooking() {
    }

    public TicketBooking(String bookingReference, User user, TrainSchedule schedule, String passengerName,
                         String contactEmail, String contactPhone, BigDecimal totalAmount, LocalDateTime holdExpiresAt) {
        this.bookingReference = bookingReference;
        this.user = user;
        this.schedule = schedule;
        this.passengerName = passengerName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.totalAmount = totalAmount;
        this.holdExpiresAt = holdExpiresAt;
    }

    public String getBookingReference() { return bookingReference; }
    public User getUser() { return user; }
    public TrainSchedule getSchedule() { return schedule; }
    public String getPassengerName() { return passengerName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }

    public void markPaymentFailed() { this.status = BookingStatus.PAYMENT_FAILED; }
    public void markPending(LocalDateTime expiresAt) { this.status = BookingStatus.PENDING_PAYMENT; this.holdExpiresAt = expiresAt; }
    public void confirm() { this.status = BookingStatus.CONFIRMED; }
    public void cancel() { this.status = BookingStatus.CANCELLED; }
    public void expire() { this.status = BookingStatus.EXPIRED; }
    public void updatePassengerDetails(String passengerName, String contactEmail, String contactPhone) {
        this.passengerName = passengerName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }
}
