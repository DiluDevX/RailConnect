package lk.sliit.railconnect.features.train.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;

public class TrainForm {
    @NotBlank(message = "Train number is required.")
    @Pattern(regexp = "\\d{4}", message = "Train number must be exactly 4 digits, for example 1001.")
    private String trainNumber;

    @NotBlank
    @Size(max = 120)
    private String trainName;

    @Size(max = 300)
    private String description;

    @NotNull(groups = Update.class, message = "Train status is required.")
    private TrainStatus status;

    public interface Update {
    }

    public static TrainForm from(Train train) {
        TrainForm form = new TrainForm();
        form.trainNumber = train.getTrainNumber();
        form.trainName = train.getTrainName();
        form.description = train.getDescription();
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

    public TrainStatus getStatus() {
        return status;
    }

    public void setStatus(TrainStatus status) {
        this.status = status;
    }
}
