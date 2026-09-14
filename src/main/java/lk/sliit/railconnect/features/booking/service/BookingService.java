package lk.sliit.railconnect.features.booking.service;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.features.booking.domain.BookingSeat;
import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.domain.Payment;
import lk.sliit.railconnect.features.booking.domain.PaymentStatus;
import lk.sliit.railconnect.features.booking.domain.PaymentMethod;
import lk.sliit.railconnect.features.booking.domain.ReservationStatus;
import lk.sliit.railconnect.features.booking.domain.SeatReservation;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.dto.BookingForm;
import lk.sliit.railconnect.features.booking.dto.SeatOption;
import lk.sliit.railconnect.features.booking.dto.CarriageSeatGroup;
import lk.sliit.railconnect.features.booking.repository.BookingSeatRepository;
import lk.sliit.railconnect.features.booking.repository.PaymentRepository;
import lk.sliit.railconnect.features.booking.repository.SeatReservationRepository;
import lk.sliit.railconnect.features.booking.repository.TicketBookingRepository;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.carriage.domain.Seat;
import lk.sliit.railconnect.features.carriage.domain.SeatStatus;
import lk.sliit.railconnect.features.carriage.repository.SeatRepository;
import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.features.schedule.service.ScheduleService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final BigDecimal RESERVATION_FEE = new BigDecimal("100.00");

    private final TicketBookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final ScheduleService scheduleService;
    private final NotificationService notificationService;
    private final long holdMinutes;

    public BookingService(TicketBookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository,
                          SeatReservationRepository reservationRepository,
                          PaymentRepository paymentRepository,
                          SeatRepository seatRepository,
                          ScheduleService scheduleService,
                          NotificationService notificationService,
                          @Value("${railconnect.booking.hold-minutes:10}") long holdMinutes) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.seatRepository = seatRepository;
        this.scheduleService = scheduleService;
        this.notificationService = notificationService;
        this.holdMinutes = holdMinutes;
    }

    @Transactional(readOnly = true)
    public List<SeatOption> seatOptions(Long scheduleId) {
        TrainSchedule schedule = scheduleService.require(scheduleId);
        LocalDateTime now = LocalDateTime.now();
        Set<Long> blocked = new HashSet<>();
        for (SeatReservation reservation : reservationRepository.findByScheduleId(scheduleId)) {
            if (reservation.blocksBooking(now)) {
                blocked.add(reservation.getSeat().getId());
            }
        }
        return seatRepository.findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(schedule.getTrain().getId())
                .stream()
                .map(seat -> new SeatOption(seat, isPhysicalSeatAvailable(seat) && !blocked.contains(seat.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarriageSeatGroup> seatGroups(Long scheduleId) {
        TrainSchedule schedule = scheduleService.require(scheduleId);
        return seatOptions(scheduleId).stream()
                .collect(Collectors.groupingBy(option -> option.seat().getCarriage(), LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new CarriageSeatGroup(entry.getKey(), entry.getValue(), schedule.getTrain().fareFor(entry.getKey().getClassType(), schedule.getBaseFare())
                        .setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    @Transactional
    public TicketBooking startBooking(User actor, Long scheduleId, BookingForm form) {
        ensureBookingActor(actor);
        TrainSchedule schedule = scheduleService.require(scheduleId);
        validateBookableSchedule(schedule);
        List<Seat> seats = seatRepository.findAllById(form.getSeatIds());
        if (seats.size() != new HashSet<>(form.getSeatIds()).size() || seats.isEmpty()) {
            throw new BusinessRuleException("One or more selected seats do not exist.");
        }
        for (Seat seat : seats) {
            validateSeatBelongsToSchedule(seat, schedule);
        }

        BigDecimal fare = seats.stream()
                .map(seat -> schedule.getTrain().fareFor(seat.getCarriage().getClassType(), schedule.getBaseFare()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(RESERVATION_FEE)
                .setScale(2, RoundingMode.HALF_UP);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(holdMinutes);
        String passengerName = actor.getRole() == UserRole.PASSENGER
                ? actor.getFullName()
                : (form.getPassengerName() == null || form.getPassengerName().isBlank() ? "Customer" : form.getPassengerName().trim());
        // Assisted counter bookings intentionally keep customer contact fields internal.
        // The schema requires values, so use non-customer placeholders and never display them to staff.
        String contactEmail = actor.getRole() == UserRole.PASSENGER ? actor.getEmail() : "counter@railconnect.local";
        String contactPhone = actor.getRole() == UserRole.PASSENGER ? actor.getPhone() : "0000000000";
        TicketBooking booking = bookingRepository.save(new TicketBooking(newBookingReference(), actor, schedule,
                passengerName, contactEmail, contactPhone, fare, expiresAt));

        try {
            for (Seat seat : seats) {
                holdSeat(schedule, seat, booking, expiresAt);
                BigDecimal seatFare = schedule.getTrain().fareFor(seat.getCarriage().getClassType(), schedule.getBaseFare())
                        .setScale(2, RoundingMode.HALF_UP);
                bookingSeatRepository.save(new BookingSeat(booking, seat, seatFare));
            }
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleException("A selected seat was reserved by another customer. Please choose again.");
        }
        return booking;
    }

    @Transactional
    public TicketBooking processPayment(Long bookingId, User actor, PaymentMethod method, boolean successful) {
        ensureBookingActor(actor);
        if (actor.getRole() == UserRole.PASSENGER && method != PaymentMethod.SIMULATED_CARD) {
            throw new BusinessRuleException("Passengers must use the simulated card checkout.");
        }
        if (actor.getRole() == UserRole.BOOKING_OFFICER && method != PaymentMethod.CASH) {
            throw new BusinessRuleException("Booking officers must record an in-person cash payment.");
        }
        TicketBooking booking = requireAccessible(bookingId, actor);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("This booking is not waiting for payment.");
        }
        if (booking.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            releaseReservations(booking);
            booking.expire();
            throw new BusinessRuleException("The seat hold expired. Retry the booking to check availability again.");
        }
        int attempt = Math.toIntExact(paymentRepository.countByBookingId(bookingId) + 1);
        PaymentStatus status = successful ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED;
        paymentRepository.save(new Payment(booking, attempt, booking.getTotalAmount(), status, method, transactionReference(method)));
        if (successful) {
            reservationRepository.findByBookingId(bookingId).forEach(SeatReservation::confirm);
            booking.confirm();
            notificationService.bookingConfirmed(booking);
        } else {
            releaseReservations(booking);
            booking.markPaymentFailed();
        }
        return booking;
    }

    @Transactional
    public TicketBooking processSimulatedPayment(Long bookingId, User actor, boolean successful) {
        return processPayment(bookingId, actor, PaymentMethod.SIMULATED_CARD, successful);
    }

    @Transactional
    public TicketBooking retry(Long bookingId, User actor) {
        TicketBooking booking = requireAccessible(bookingId, actor);
        if (booking.getStatus() != BookingStatus.PAYMENT_FAILED && booking.getStatus() != BookingStatus.EXPIRED) {
            throw new BusinessRuleException("Only failed or expired bookings can be retried.");
        }
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(holdMinutes);
        for (BookingSeat bookingSeat : bookingSeatRepository.findByBookingIdOrderBySeatCarriageCarriageNumberAscSeatSeatNumberAsc(bookingId)) {
            validateSeatBelongsToSchedule(bookingSeat.getSeat(), booking.getSchedule());
            holdSeat(booking.getSchedule(), bookingSeat.getSeat(), booking, expiresAt);
        }
        booking.markPending(expiresAt);
        return booking;
    }

    @Transactional
    public void cancel(Long bookingId, User actor) {
        TicketBooking booking = requireAccessible(bookingId, actor);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED && !booking.getSchedule().getTravelDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("A booking cannot be cancelled on or after its travel date.");
        }
        paymentRepository.findFirstByBookingIdAndStatusOrderByAttemptNumberDesc(bookingId, PaymentStatus.SUCCEEDED)
                .ifPresent(Payment::refund);
        releaseReservations(booking);
        booking.cancel();
        notificationService.bookingCancelled(booking);
    }

    @Transactional(readOnly = true)
    public TicketBooking requireAccessible(Long bookingId, User actor) {
        TicketBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking was not found."));
        if (actor.getRole() == UserRole.PASSENGER && !booking.getUser().getId().equals(actor.getId())) {
            throw new ResourceNotFoundException("Booking was not found.");
        }
        return booking;
    }

    @Transactional(readOnly = true)
    public List<TicketBooking> forUser(User user) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<TicketBooking> all() { return bookingRepository.findAllByOrderByCreatedAtDesc(); }

    @Transactional(readOnly = true)
    public List<BookingSeat> seatsForBooking(Long bookingId) {
        return bookingSeatRepository.findByBookingIdOrderBySeatCarriageCarriageNumberAscSeatSeatNumberAsc(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Payment> paymentsForBooking(Long bookingId) {
        return paymentRepository.findByBookingIdOrderByAttemptNumber(bookingId);
    }

    private void holdSeat(TrainSchedule schedule, Seat seat, TicketBooking booking, LocalDateTime expiresAt) {
        SeatReservation reservation = reservationRepository.lockForScheduleAndSeat(schedule.getId(), seat.getId()).orElse(null);
        if (reservation == null) {
            reservationRepository.saveAndFlush(new SeatReservation(schedule, seat, booking, expiresAt));
            return;
        }
        if (reservation.blocksBooking(LocalDateTime.now())
                && (reservation.getBooking() == null || !reservation.getBooking().getId().equals(booking.getId()))) {
            throw new BusinessRuleException("Seat " + seat.getSeatNumber() + " is no longer available.");
        }
        reservation.holdFor(booking, expiresAt);
    }

    private void releaseReservations(TicketBooking booking) {
        reservationRepository.findByBookingId(booking.getId()).forEach(SeatReservation::release);
    }

    private void validateBookableSchedule(TrainSchedule schedule) {
        if (schedule.getStatus() != ScheduleStatus.ACTIVE && schedule.getStatus() != ScheduleStatus.DELAYED) {
            throw new BusinessRuleException("This schedule is not available for booking.");
        }
        if (schedule.getTravelDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Past schedules cannot be booked.");
        }
    }

    private void validateSeatBelongsToSchedule(Seat seat, TrainSchedule schedule) {
        if (!seat.getCarriage().getTrain().getId().equals(schedule.getTrain().getId())) {
            throw new BusinessRuleException("A selected seat does not belong to the scheduled train.");
        }
        if (!isPhysicalSeatAvailable(seat)) {
            throw new BusinessRuleException("Seat " + seat.getSeatNumber() + " is out of service.");
        }
    }

    private boolean isPhysicalSeatAvailable(Seat seat) {
        return seat.getStatus() == SeatStatus.ACTIVE && seat.getCarriage().getStatus() == CarriageStatus.ACTIVE;
    }

    private String newBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private void ensureBookingActor(User actor) {
        if (actor.getRole() != UserRole.PASSENGER && actor.getRole() != UserRole.BOOKING_OFFICER) {
            throw new BusinessRuleException("Only passengers and booking officers can create or pay for bookings.");
        }
    }

    private String transactionReference(PaymentMethod method) {
        String prefix = method == PaymentMethod.CASH ? "CASH-" : "CARD-";
        return prefix + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
