package lk.sliit.railconnect.features.train.domain;

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
@Table(name = "trains")
public class Train extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String trainNumber;

    @Column(nullable = false, length = 120)
    private String trainName;

    @Column(length = 300)
    private String description;

    @Column(name = "first_class_fare", precision = 10, scale = 2)
    private BigDecimal firstClassFare;

    @Column(name = "second_class_fare", precision = 10, scale = 2)
    private BigDecimal secondClassFare;

    @Column(name = "third_class_fare", precision = 10, scale = 2)
    private BigDecimal thirdClassFare;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TrainStatus status = TrainStatus.ACTIVE;

    protected Train() {
    }

    public Train(String trainNumber, String trainName, String description) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.description = description;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getFirstClassFare() { return firstClassFare; }
    public BigDecimal getSecondClassFare() { return secondClassFare; }
    public BigDecimal getThirdClassFare() { return thirdClassFare; }

    public TrainStatus getStatus() {
        return status;
    }

    public BigDecimal fareFor(lk.sliit.railconnect.features.carriage.domain.CarriageClass carriageClass,
                              BigDecimal scheduleBaseFare) {
        BigDecimal configured = switch (carriageClass) {
            case FIRST -> firstClassFare;
            case SECOND -> secondClassFare;
            case THIRD -> thirdClassFare;
        };
        return configured == null || configured.signum() == 0
                ? scheduleBaseFare.multiply(carriageClass.getFareMultiplier())
                : configured;
    }

    public void update(String trainNumber, String trainName, String description,
                       BigDecimal firstClassFare, BigDecimal secondClassFare, BigDecimal thirdClassFare,
                       TrainStatus status) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.description = description;
        this.firstClassFare = firstClassFare;
        this.secondClassFare = secondClassFare;
        this.thirdClassFare = thirdClassFare;
        this.status = status;
    }
}
