package lk.sliit.railconnect.features.train.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance")
public class Maintenance extends BaseEntity {
    @Column(name = "maintenance_type", nullable = false, length = 80)
    private String maintenanceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maintenance_train"))
    private Train train;

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    protected Maintenance() {
    }

    public Maintenance(String maintenanceType, Train train, LocalDate maintenanceDate,
                       String description, MaintenanceStatus status) {
        update(maintenanceType, train, maintenanceDate, description, status);
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public Train getTrain() {
        return train;
    }

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public String getDescription() {
        return description;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void update(String maintenanceType, Train train, LocalDate maintenanceDate,
                       String description, MaintenanceStatus status) {
        this.maintenanceType = maintenanceType;
        this.train = train;
        this.maintenanceDate = maintenanceDate;
        this.description = description;
        this.status = status;
    }

    public void cancel() {
        this.status = MaintenanceStatus.CANCELLED;
    }
}
