package lk.sliit.railconnect.features.booking;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.repository.UserRepository;
import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.domain.PaymentMethod;
import lk.sliit.railconnect.features.booking.domain.ReservationStatus;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.dto.BookingForm;
import lk.sliit.railconnect.features.booking.repository.PaymentRepository;
import lk.sliit.railconnect.features.booking.repository.SeatReservationRepository;
import lk.sliit.railconnect.features.booking.repository.TicketBookingRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingExpiryIntegrationTest {
    @Autowired BookingService bookingService;
    @Autowired CarriageService carriageService;
    @Autowired UserRepository userRepository;
    @Autowired TrainRepository trainRepository;
    @Autowired RouteRepository routeRepository;
    @Autowired TrainScheduleRepository scheduleRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TicketBookingRepository bookingRepository;
    @Autowired SeatReservationRepository reservationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void expiredPaymentCommitsReleasedSeatsAndAllowsRetry() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Fixture fixture = transaction.execute(status -> {
            User passenger = userRepository.save(new User("Expiry Tester", "expiry-test@example.com",
                    "encoded", "0771234567", UserRole.PASSENGER));
            Train train = trainRepository.save(new Train("T-EXPIRY", "Expiry Test", null));
            Route route = routeRepository.save(new Route("R-EXPIRY", "Colombo", "Kandy", new BigDecimal("120")));
            TrainSchedule schedule = scheduleRepository.save(new TrainSchedule("S-EXPIRY", train, route,
                    LocalDate.now().plusDays(5), LocalTime.of(8, 0), LocalTime.of(10, 0),
                    new BigDecimal("1500"), new BigDecimal("1000"), new BigDecimal("750")));
            CarriageForm carriage = new CarriageForm();
            carriage.setTrainId(train.getId());
            carriage.setCarriageNumber("A01");
            carriage.setClassType(CarriageClass.SECOND);
            carriage.setCapacity(4);
            carriageService.create(carriage);
            Long seatId = seatRepository.findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(
                    train.getId()).get(0).getId();
            BookingForm form = new BookingForm();
            form.setSeatIds(List.of(seatId));
            TicketBooking booking = bookingService.startBooking(passenger, schedule.getId(), form);
            booking.markPending(LocalDateTime.now().minusMinutes(1));
            return new Fixture(passenger, booking.getId());
        });
        assertThat(fixture).isNotNull();

        // No enclosing test transaction: the service must commit before we reload its result.
        TicketBooking expired = bookingService.processPayment(
                fixture.bookingId(), fixture.passenger(), PaymentMethod.SIMULATED_CARD, true);
        assertThat(expired.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        transaction.executeWithoutResult(status -> {
            assertThat(bookingRepository.findById(fixture.bookingId()).orElseThrow().getStatus())
                    .isEqualTo(BookingStatus.EXPIRED);
            assertThat(reservationRepository.findByBookingId(fixture.bookingId()))
                    .singleElement().extracting("status").isEqualTo(ReservationStatus.RELEASED);
            assertThat(paymentRepository.countByBookingId(fixture.bookingId())).isZero();
        });

        TicketBooking retried = bookingService.retry(fixture.bookingId(), fixture.passenger());
        assertThat(retried.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        TicketBooking confirmed = bookingService.processPayment(
                fixture.bookingId(), fixture.passenger(), PaymentMethod.SIMULATED_CARD, true);
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    private record Fixture(User passenger, Long bookingId) {
    }
}
