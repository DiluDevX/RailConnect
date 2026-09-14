package lk.sliit.railconnect.features.schedule.service;

import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.domain.RouteStatus;
import lk.sliit.railconnect.features.route.service.RouteService;
import lk.sliit.railconnect.features.booking.repository.SeatReservationRepository;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.carriage.domain.SeatStatus;
import lk.sliit.railconnect.features.carriage.repository.SeatRepository;
import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.features.schedule.dto.ScheduleForm;
import lk.sliit.railconnect.features.schedule.repository.TrainScheduleRepository;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private static final List<ScheduleStatus> OPERATING_STATUSES = List.of(ScheduleStatus.ACTIVE, ScheduleStatus.DELAYED);

    private final TrainScheduleRepository scheduleRepository;
    private final TrainService trainService;
    private final RouteService routeService;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository reservationRepository;

    public ScheduleService(TrainScheduleRepository scheduleRepository, TrainService trainService, RouteService routeService,
                           SeatRepository seatRepository, SeatReservationRepository reservationRepository) {
        this.scheduleRepository = scheduleRepository;
        this.trainService = trainService;
        this.routeService = routeService;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainSchedule> list() {
        return scheduleRepository.findAllByOrderByTravelDateAscDepartureTimeAsc();
    }

    @Transactional(readOnly = true)
    public TrainSchedule require(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train schedule was not found."));
    }

    @Transactional(readOnly = true)
    public List<TrainSchedule> search(String from, String to, LocalDate date) {
        return search(from, to, date, 1);
    }

    @Transactional(readOnly = true)
    public List<TrainSchedule> search(String from, String to, LocalDate date, int passengers) {
        if (from == null || from.isBlank() || to == null || to.isBlank() || date == null) {
            return List.of();
        }
        int requestedPassengers = Math.max(1, Math.min(passengers, 10));
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.search(from.trim(), to.trim(), date, OPERATING_STATUSES).stream()
                .filter(schedule -> availableSeats(schedule.getTrain().getId(), schedule.getId(), now) >= requestedPassengers)
                .toList();
    }

    private long availableSeats(Long trainId, Long scheduleId, LocalDateTime now) {
        long physicalSeats = seatRepository.findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(trainId).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.ACTIVE && seat.getCarriage().getStatus() == CarriageStatus.ACTIVE)
                .count();
        Set<Long> reservedSeatIds = reservationRepository.findByScheduleId(scheduleId).stream()
                .filter(reservation -> reservation.blocksBooking(now))
                .map(reservation -> reservation.getSeat().getId())
                .collect(Collectors.toCollection(HashSet::new));
        return Math.max(0, physicalSeats - reservedSeatIds.size());
    }

    @Transactional
    public TrainSchedule create(ScheduleForm form) {
        validate(form, null);
        Train train = trainService.require(form.getTrainId());
        Route route = routeService.require(form.getRouteId());
        return scheduleRepository.save(new TrainSchedule(form.getScheduleCode().trim(), train, route,
                form.getTravelDate(), form.getDepartureTime(), form.getArrivalTime(), form.getBaseFare()));
    }

    @Transactional
    public TrainSchedule update(Long id, ScheduleForm form) {
        TrainSchedule schedule = require(id);
        validate(form, id);
        schedule.update(form.getScheduleCode().trim(), trainService.require(form.getTrainId()),
                routeService.require(form.getRouteId()), form.getTravelDate(), form.getDepartureTime(),
                form.getArrivalTime(), form.getBaseFare(), form.getStatus());
        return schedule;
    }

    @Transactional
    public void cancel(Long id) {
        require(id).cancel();
    }

    private void validate(ScheduleForm form, Long id) {
        if (!form.getDepartureTime().isBefore(form.getArrivalTime())) {
            throw new BusinessRuleException("Departure time must be before arrival time.");
        }
        Train train = trainService.require(form.getTrainId());
        Route route = routeService.require(form.getRouteId());
        if (train.getStatus() != TrainStatus.ACTIVE || route.getStatus() != RouteStatus.ACTIVE) {
            throw new BusinessRuleException("Only active trains and routes can be scheduled.");
        }
        boolean duplicate = id == null
                ? scheduleRepository.existsByScheduleCodeIgnoreCase(form.getScheduleCode().trim())
                : scheduleRepository.existsByScheduleCodeIgnoreCaseAndIdNot(form.getScheduleCode().trim(), id);
        if (duplicate) {
            throw new BusinessRuleException("Schedule code already exists.");
        }
        if (scheduleRepository.countConflicts(form.getTrainId(), form.getTravelDate(), form.getDepartureTime(),
                form.getArrivalTime(), id, OPERATING_STATUSES) > 0) {
            throw new BusinessRuleException("This train already has an overlapping schedule.");
        }
    }
}
