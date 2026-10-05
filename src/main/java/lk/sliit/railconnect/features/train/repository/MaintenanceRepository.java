package lk.sliit.railconnect.features.train.repository;

import lk.sliit.railconnect.features.train.domain.Maintenance;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    long countByStatus(MaintenanceStatus status);

    @Query("SELECT m FROM Maintenance m JOIN FETCH m.train WHERE m.id = :id")
    Optional<Maintenance> findDetailedById(@Param("id") Long id);

    @Query("SELECT m FROM Maintenance m JOIN FETCH m.train t WHERE t.id = :trainId ORDER BY m.maintenanceDate DESC, m.id DESC")
    List<Maintenance> findHistoryByTrainId(@Param("trainId") Long trainId);

    @Query("""
            SELECT m FROM Maintenance m JOIN FETCH m.train t
            WHERE (:trainId IS NULL OR t.id = :trainId)
            AND (:status IS NULL OR m.status = :status)
            AND (:type IS NULL OR :type = '' OR LOWER(m.maintenanceType) LIKE LOWER(CONCAT('%', :type, '%')))
            AND (:maintenanceDate IS NULL OR m.maintenanceDate = :maintenanceDate)
            ORDER BY m.maintenanceDate DESC, m.id DESC
            """)
    List<Maintenance> search(@Param("trainId") Long trainId,
                             @Param("status") MaintenanceStatus status,
                             @Param("type") String type,
                             @Param("maintenanceDate") LocalDate maintenanceDate);
}
