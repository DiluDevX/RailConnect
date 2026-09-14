package lk.sliit.railconnect.features.carriage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"carriage_id", "seat_number"}))
public class Seat extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carriage_id", nullable = false)
    private Carriage carriage;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.ACTIVE;

    protected Seat() {
    }

    public Seat(Carriage carriage, String seatNumber, SeatType seatType) {
        this.carriage = carriage;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public Carriage getCarriage() { return carriage; }
    public String getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public SeatStatus getStatus() { return status; }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
