package lk.sliit.railconnect.features.schedule.repository;

import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    boolean existsByScheduleCodeIgnoreCase(String scheduleCode);
    boolean existsByScheduleCodeIgnoreCaseAndIdNot(String scheduleCode, Long id);
    @EntityGraph(attributePaths = {"train", "route"})
    List<TrainSchedule> findAllByOrderByTravelDateAscDepartureTimeAsc();

    @Override
    @EntityGraph(attributePaths = {"train", "route"})
    Optional<TrainSchedule> findById(Long id);
    long countByStatusAndTravelDate(ScheduleStatus status, LocalDate date);

    Optional<TrainSchedule> findFirstByStatusAndTravelDateGreaterThanEqualOrderByTravelDateAscDepartureTimeAsc(
            ScheduleStatus status, LocalDate date);

    @Query("""
            select s from TrainSchedule s
            join fetch s.train
            join fetch s.route
            where s.status in :statuses
              and lower(s.route.startStation) like lower(concat('%', :from, '%'))
              and lower(s.route.endStation) like lower(concat('%', :to, '%'))
              and s.travelDate = :date
            order by s.departureTime
            """)
    List<TrainSchedule> search(@Param("from") String from,
                               @Param("to") String to,
                               @Param("date") LocalDate date,
                               @Param("statuses") List<ScheduleStatus> statuses);

    @Query("""
            select count(s) from TrainSchedule s
            where s.train.id = :trainId and s.travelDate = :date
              and s.status in :statuses and (:excludeId is null or s.id <> :excludeId)
              and s.departureTime < :arrival and s.arrivalTime > :departure
            """)
    long countConflicts(@Param("trainId") Long trainId,
                        @Param("date") LocalDate date,
                        @Param("departure") LocalTime departure,
                        @Param("arrival") LocalTime arrival,
                        @Param("excludeId") Long excludeId,
                        @Param("statuses") List<ScheduleStatus> statuses);
}
