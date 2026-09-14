package lk.sliit.railconnect.features.booking.repository;

import lk.sliit.railconnect.features.booking.domain.BookingSeat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    @EntityGraph(attributePaths = {"booking", "seat", "seat.carriage"})
    List<BookingSeat> findByBookingIdOrderBySeatCarriageCarriageNumberAscSeatSeatNumberAsc(Long bookingId);
    boolean existsBySeatId(Long seatId);
}
