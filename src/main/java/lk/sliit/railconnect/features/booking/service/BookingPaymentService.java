package lk.sliit.railconnect.features.booking.service;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.domain.Payment;
import lk.sliit.railconnect.features.booking.domain.PaymentMethod;
import lk.sliit.railconnect.features.booking.domain.PaymentStatus;
import lk.sliit.railconnect.features.booking.domain.SeatReservation;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.repository.PaymentRepository;
import lk.sliit.railconnect.features.booking.repository.SeatReservationRepository;
import lk.sliit.railconnect.features.booking.repository.TicketBookingRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class BookingPaymentService {
    private final TicketBookingRepository bookingRepository;
    private final SeatReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public BookingPaymentService(TicketBookingRepository bookingRepository, SeatReservationRepository reservationRepository,
                                 PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
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
        TicketBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessRuleException("Booking was not found."));
        if (actor.getRole() == UserRole.PASSENGER && !booking.getUser().getId().equals(actor.getId())) {
            throw new BusinessRuleException("Booking was not found.");
        }
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
        } else {
            releaseReservations(booking);
            booking.markPaymentFailed();
        }
        return booking;
    }

    public TicketBooking processSimulatedPayment(Long bookingId, User actor, boolean successful) {
        return processPayment(bookingId, actor, PaymentMethod.SIMULATED_CARD, successful);
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

    private void releaseReservations(TicketBooking booking) {
        reservationRepository.findByBookingId(booking.getId()).forEach(SeatReservation::release);
    }
}
