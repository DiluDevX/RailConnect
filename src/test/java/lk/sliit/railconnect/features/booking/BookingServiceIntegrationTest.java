package lk.sliit.railconnect.features.booking;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.repository.UserRepository;
import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.domain.PaymentStatus;
import lk.sliit.railconnect.features.booking.domain.PaymentMethod;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.dto.BookingForm;
import lk.sliit.railconnect.features.booking.repository.PaymentRepository;
import lk.sliit.railconnect.features.booking.service.BookingService;
import lk.sliit.railconnect.features.carriage.domain.CarriageClass;
import lk.sliit.railconnect.features.carriage.dto.CarriageForm;
import lk.sliit.railconnect.features.carriage.repository.SeatRepository;
import lk.sliit.railconnect.features.carriage.service.CarriageService;
import lk.sliit.railconnect.features.route.domain.Route;
import lk.sliit.railconnect.features.route.repository.RouteRepository;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.features.schedule.repository.TrainScheduleRepository;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {
    @Autowired BookingService bookingService;
    @Autowired CarriageService carriageService;
    @Autowired UserRepository userRepository;
    @Autowired TrainRepository trainRepository;
    @Autowired RouteRepository routeRepository;
    @Autowired TrainScheduleRepository scheduleRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired PaymentRepository paymentRepository;

    private User passenger;
    private TrainSchedule schedule;
    private Long seatId;

    @BeforeEach
    void setUp() {
        passenger = userRepository.save(new User("Test Passenger", "booking-test@example.com", "encoded", "0771234567", UserRole.PASSENGER));
        Train train = trainRepository.save(new Train("T-BOOK", "Booking Test", null));
        Route route = routeRepository.save(new Route("R-BOOK", "Colombo", "Kandy", new BigDecimal("120")));
        schedule = scheduleRepository.save(new TrainSchedule("S-BOOK", train, route, LocalDate.now().plusDays(5),
                LocalTime.of(8, 0), LocalTime.of(10, 0), new BigDecimal("1000")));
        CarriageForm carriageForm = new CarriageForm();
        carriageForm.setTrainId(train.getId());
        carriageForm.setCarriageNumber("A01");
        carriageForm.setClassType(CarriageClass.SECOND);
        carriageForm.setCapacity(4);
        carriageService.create(carriageForm);
        seatId = seatRepository.findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(train.getId()).get(0).getId();
    }

    @Test
    void confirmsTheExistingPendingBookingAfterSuccessfulPayment() {
        TicketBooking pending = bookingService.startBooking(passenger, schedule.getId(), bookingForm());
        Long originalId = pending.getId();

        TicketBooking confirmed = bookingService.processSimulatedPayment(originalId, passenger, true);

        assertThat(confirmed.getId()).isEqualTo(originalId);
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(paymentRepository.findByBookingIdOrderByAttemptNumber(originalId))
                .singleElement().extracting("status").isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(paymentRepository.findByBookingIdOrderByAttemptNumber(originalId))
                .singleElement().extracting("method").isEqualTo(PaymentMethod.SIMULATED_CARD);
    }

    @Test
    void failedPaymentReleasesSeatAndRetryReusesBooking() {
        TicketBooking booking = bookingService.startBooking(passenger, schedule.getId(), bookingForm());
        bookingService.processSimulatedPayment(booking.getId(), passenger, false);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);

        TicketBooking retried = bookingService.retry(booking.getId(), passenger);

        assertThat(retried.getId()).isEqualTo(booking.getId());
        assertThat(retried.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(retried.getHoldExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void bookingOfficerRecordsCashButPassengerCannot() {
        User officer = userRepository.save(new User("Test Officer", "officer-test@example.com", "encoded",
                "0712345678", UserRole.BOOKING_OFFICER));
        BookingForm assistedForm = bookingForm();
        assistedForm.setPassengerName("Counter Customer");
        assistedForm.setContactEmail("counter@example.com");
        assistedForm.setContactPhone("0755555555");
        TicketBooking booking = bookingService.startBooking(officer, schedule.getId(), assistedForm);

        TicketBooking confirmed = bookingService.processPayment(booking.getId(), officer, PaymentMethod.CASH, true);

        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(paymentRepository.findByBookingIdOrderByAttemptNumber(booking.getId()))
                .singleElement().extracting("method").isEqualTo(PaymentMethod.CASH);
    }

    @Test
    void administratorCannotCreateCustomerBooking() {
        User administrator = userRepository.save(new User("Test Admin", "admin-test@example.com", "encoded",
                "0700000000", UserRole.RAILWAY_ADMIN));

        assertThatThrownBy(() -> bookingService.startBooking(administrator, schedule.getId(), bookingForm()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only passengers and booking officers");
    }

    private BookingForm bookingForm() {
        BookingForm form = new BookingForm();
        form.setSeatIds(List.of(seatId));
        form.setPassengerName(passenger.getFullName());
        form.setContactEmail(passenger.getEmail());
        form.setContactPhone(passenger.getPhone());
        return form;
    }
}
