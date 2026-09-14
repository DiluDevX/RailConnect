package lk.sliit.railconnect.features.carriage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.carriage.domain.Carriage;
import lk.sliit.railconnect.features.carriage.domain.CarriageClass;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;

public class CarriageForm {
    @NotNull private Long trainId;
    @NotBlank @Size(max = 20) private String carriageNumber;
    @NotNull private CarriageClass classType;
    @Min(4) @Max(120) private int capacity = 20;
    private CarriageStatus status = CarriageStatus.ACTIVE;

    public static CarriageForm from(Carriage carriage) {
        CarriageForm form = new CarriageForm();
        form.trainId = carriage.getTrain().getId();
        form.carriageNumber = carriage.getCarriageNumber();
        form.classType = carriage.getClassType();
        form.capacity = carriage.getCapacity();
        form.status = carriage.getStatus();
        return form;
    }

    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public String getCarriageNumber() { return carriageNumber; }
    public void setCarriageNumber(String carriageNumber) { this.carriageNumber = carriageNumber; }
    public CarriageClass getClassType() { return classType; }
    public void setClassType(CarriageClass classType) { this.classType = classType; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public CarriageStatus getStatus() { return status; }
    public void setStatus(CarriageStatus status) { this.status = status; }
}
