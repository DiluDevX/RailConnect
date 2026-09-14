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
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "carriages", uniqueConstraints = @UniqueConstraint(columnNames = {"train_id", "carriage_number"}))
public class Carriage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "carriage_number", nullable = false, length = 20)
    private String carriageNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CarriageClass classType;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CarriageStatus status = CarriageStatus.ACTIVE;

    protected Carriage() {
    }

    public Carriage(Train train, String carriageNumber, CarriageClass classType, int capacity) {
        this.train = train;
        this.carriageNumber = carriageNumber;
        this.classType = classType;
        this.capacity = capacity;
    }

    public Train getTrain() { return train; }
    public String getCarriageNumber() { return carriageNumber; }
    public CarriageClass getClassType() { return classType; }
    public int getCapacity() { return capacity; }
    public CarriageStatus getStatus() { return status; }

    public void update(String carriageNumber, CarriageClass classType, CarriageStatus status) {
        this.carriageNumber = carriageNumber;
        this.classType = classType;
        this.status = status;
    }
}
