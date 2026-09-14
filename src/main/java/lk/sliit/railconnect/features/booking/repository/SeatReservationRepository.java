package lk.sliit.railconnect.features.booking.repository;

import jakarta.persistence.LockModeType;
import lk.sliit.railconnect.features.booking.domain.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from SeatReservation r where r.schedule.id = :scheduleId and r.seat.id = :seatId")
    Optional<SeatReservation> lockForScheduleAndSeat(@Param("scheduleId") Long scheduleId, @Param("seatId") Long seatId);

    List<SeatReservation> findByScheduleId(Long scheduleId);
    List<SeatReservation> findByBookingId(Long bookingId);
}
