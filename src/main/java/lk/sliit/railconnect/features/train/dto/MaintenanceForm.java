package lk.sliit.railconnect.features.train.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.train.domain.Maintenance;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class MaintenanceForm {
    @NotBlank(message = "Maintenance type is required.")
    @Size(max = 80, message = "Maintenance type must not exceed 80 characters.")
    private String maintenanceType;

    @NotNull(message = "Train is required.")
    private Long trainId;

    @NotNull(message = "Maintenance date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maintenanceDate;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    private String description;

    @NotNull(message = "Status is required.")
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    public static MaintenanceForm from(Maintenance maintenance) {
        MaintenanceForm form = new MaintenanceForm();
        form.maintenanceType = maintenance.getMaintenanceType();
        form.trainId = maintenance.getTrain().getId();
        form.maintenanceDate = maintenance.getMaintenanceDate();
        form.description = maintenance.getDescription();
        form.status = maintenance.getStatus();
        return form;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(LocalDate maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }
}
