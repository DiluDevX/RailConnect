package com.railconnect.schedule_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @NotNull(message = "Train ID is required")
    @Column(nullable = false)
    private Long trainId;

    @NotNull(message = "Route ID is required")
    @Column(nullable = false)
    private Long routeId;

    @NotNull(message = "Travel date is required")
    @FutureOrPresent(message = "Travel date cannot be in the past")
    @Column(nullable = false)
    private LocalDate travelDate;

    @NotNull(message = "Departure time is required")
    @Column(nullable = false)
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Column(nullable = false)
    private LocalTime arrivalTime;

    @NotNull(message = "Base fare is required")
    @Positive(message = "Base fare must be greater than 0")
    @Column(nullable = false)
    private BigDecimal baseFare;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;
}