package lk.sliit.railconnect.features.schedule.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleForm {
    @NotBlank @Size(max = 40)
    private String scheduleCode;
    @NotNull private Long trainId;
    @NotNull private Long routeId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate travelDate;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime departureTime;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime arrivalTime;
    @NotNull @DecimalMin("0.0")
    private BigDecimal firstClassFare;
    @NotNull @DecimalMin("0.0")
    private BigDecimal secondClassFare;
    @NotNull @DecimalMin("0.0")
    private BigDecimal thirdClassFare;
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    public static ScheduleForm from(TrainSchedule schedule) {
        ScheduleForm form = new ScheduleForm();
        form.scheduleCode = schedule.getScheduleCode();
        form.trainId = schedule.getTrain().getId();
        form.routeId = schedule.getRoute().getId();
        form.travelDate = schedule.getTravelDate();
        form.departureTime = schedule.getDepartureTime();
        form.arrivalTime = schedule.getArrivalTime();
        form.firstClassFare = schedule.getFirstClassFare();
        form.secondClassFare = schedule.getSecondClassFare();
        form.thirdClassFare = schedule.getThirdClassFare();
        form.status = schedule.getStatus();
        return form;
    }

    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public BigDecimal getFirstClassFare() { return firstClassFare; }
    public void setFirstClassFare(BigDecimal firstClassFare) { this.firstClassFare = firstClassFare; }
    public BigDecimal getSecondClassFare() { return secondClassFare; }
    public void setSecondClassFare(BigDecimal secondClassFare) { this.secondClassFare = secondClassFare; }
    public BigDecimal getThirdClassFare() { return thirdClassFare; }
    public void setThirdClassFare(BigDecimal thirdClassFare) { this.thirdClassFare = thirdClassFare; }
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
}
