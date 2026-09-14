package lk.sliit.railconnect.features.booking.repository;

import lk.sliit.railconnect.features.booking.domain.Payment;
import lk.sliit.railconnect.features.booking.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    long countByBookingId(Long bookingId);
    List<Payment> findByBookingIdOrderByAttemptNumber(Long bookingId);
    Optional<Payment> findFirstByBookingIdAndStatusOrderByAttemptNumberDesc(Long bookingId, PaymentStatus status);
}
