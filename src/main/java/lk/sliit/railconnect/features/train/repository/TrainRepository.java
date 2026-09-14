package lk.sliit.railconnect.features.train.repository;

import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {
    boolean existsByTrainNumberIgnoreCase(String trainNumber);
    boolean existsByTrainNumberIgnoreCaseAndIdNot(String trainNumber, Long id);
    List<Train> findByTrainNumberContainingIgnoreCaseOrTrainNameContainingIgnoreCaseOrderByTrainNumber(String number, String name);
    List<Train> findAllByOrderByTrainNumber();
    List<Train> findByStatusOrderByTrainNumber(TrainStatus status);
    long countByStatus(TrainStatus status);
}
