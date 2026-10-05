package lk.sliit.railconnect.features.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private TicketBooking booking;

    @Column(nullable = false)
    private int attemptNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionReference;

    protected Payment() {
    }

    public Payment(TicketBooking booking, int attemptNumber, BigDecimal amount, PaymentStatus status,
                   PaymentMethod method, String transactionReference) {
        this.booking = booking;
        this.attemptNumber = attemptNumber;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.transactionReference = transactionReference;
    }

    public TicketBooking getBooking() { return booking; }
    public int getAttemptNumber() { return attemptNumber; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public PaymentMethod getMethod() { return method; }
    public String getTransactionReference() { return transactionReference; }
    public void refund() { this.status = PaymentStatus.REFUNDED; }
}
