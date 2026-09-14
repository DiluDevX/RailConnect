package lk.sliit.railconnect.features.train.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class TrainForm {
    @NotBlank
    @Size(max = 30)
    private String trainNumber;

    @NotBlank
    @Size(max = 120)
    private String trainName;

    @Size(max = 300)
    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal firstClassFare;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal secondClassFare;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal thirdClassFare;

    private TrainStatus status = TrainStatus.ACTIVE;

    public static TrainForm from(Train train) {
        TrainForm form = new TrainForm();
        form.trainNumber = train.getTrainNumber();
        form.trainName = train.getTrainName();
        form.description = train.getDescription();
        form.firstClassFare = train.getFirstClassFare();
        form.secondClassFare = train.getSecondClassFare();
        form.thirdClassFare = train.getThirdClassFare();
        form.status = train.getStatus();
        return form;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getFirstClassFare() { return firstClassFare; }
    public void setFirstClassFare(BigDecimal firstClassFare) { this.firstClassFare = firstClassFare; }
    public BigDecimal getSecondClassFare() { return secondClassFare; }
    public void setSecondClassFare(BigDecimal secondClassFare) { this.secondClassFare = secondClassFare; }
    public BigDecimal getThirdClassFare() { return thirdClassFare; }
    public void setThirdClassFare(BigDecimal thirdClassFare) { this.thirdClassFare = thirdClassFare; }

    public TrainStatus getStatus() {
        return status;
    }

    public void setStatus(TrainStatus status) {
        this.status = status;
    }
}
