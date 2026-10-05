package lk.sliit.railconnect.features.carriage.repository;

import lk.sliit.railconnect.features.carriage.domain.Seat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @EntityGraph(attributePaths = {"carriage", "carriage.train"})
    List<Seat> findByCarriageIdOrderBySeatNumber(Long carriageId);

    @EntityGraph(attributePaths = {"carriage", "carriage.train"})
    List<Seat> findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(Long trainId);
}
