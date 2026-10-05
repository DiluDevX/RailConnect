package lk.sliit.railconnect.features.carriage.repository;

import lk.sliit.railconnect.features.carriage.domain.Carriage;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarriageRepository extends JpaRepository<Carriage, Long> {
    boolean existsByTrainIdAndCarriageNumberIgnoreCase(Long trainId, String carriageNumber);
    boolean existsByTrainIdAndCarriageNumberIgnoreCaseAndIdNot(Long trainId, String carriageNumber, Long id);
    @EntityGraph(attributePaths = "train")
    List<Carriage> findAllByOrderByTrainTrainNumberAscCarriageNumberAsc();

    @EntityGraph(attributePaths = "train")
    List<Carriage> findByTrainIdOrderByCarriageNumber(Long trainId);

    @Override
    @EntityGraph(attributePaths = "train")
    Optional<Carriage> findById(Long id);
    long countByStatus(CarriageStatus status);

    @Query("select coalesce(sum(c.capacity), 0) from Carriage c where c.train.id = :trainId and c.status = :status")
    long capacityForTrainAndStatus(@Param("trainId") Long trainId, @Param("status") CarriageStatus status);
}
