package lk.sliit.railconnect.features.booking.repository;

import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketBookingRepository extends JpaRepository<TicketBooking, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "schedule", "schedule.train", "schedule.route"})
    Optional<TicketBooking> findById(Long id);

    @EntityGraph(attributePaths = {"user", "schedule", "schedule.train", "schedule.route"})
    Optional<TicketBooking> findByBookingReferenceIgnoreCase(String reference);

    @EntityGraph(attributePaths = {"user", "schedule", "schedule.train", "schedule.route"})
    List<TicketBooking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "schedule", "schedule.train", "schedule.route"})
    List<TicketBooking> findAllByOrderByCreatedAtDesc();
    long countByStatus(BookingStatus status);
    long countByScheduleIdAndStatus(Long scheduleId, BookingStatus status);
}
