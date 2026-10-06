package lk.sliit.railconnect.features.train.repository;

import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {
    boolean existsByTrainNumberIgnoreCase(String trainNumber);
    boolean existsByTrainNumberIgnoreCaseAndIdNot(String trainNumber, Long id);
    List<Train> findByTrainNumberContainingIgnoreCaseOrTrainNameContainingIgnoreCaseOrderByTrainNumber(String number, String name);
    List<Train> findAllByOrderByTrainNumber();
    List<Train> findByStatusOrderByTrainNumber(TrainStatus status);
    long countByStatus(TrainStatus status);

    @Query("""
            SELECT t FROM Train t
            WHERE (:status IS NULL OR t.status = :status)
            AND (:query IS NULL OR :query = ''
                OR LOWER(t.trainNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(t.trainName) LIKE LOWER(CONCAT('%', :query, '%')))
            ORDER BY t.trainNumber
            """)
    List<Train> search(@Param("query") String query, @Param("status") TrainStatus status);
}
