package lk.sliit.railconnect.features.complaint.repository;

import lk.sliit.railconnect.features.complaint.domain.Complaint;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "booking"})
    Optional<Complaint> findById(Long id);

    @EntityGraph(attributePaths = {"user", "booking"})
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "booking"})
    List<Complaint> findAllByOrderByCreatedAtDesc();
    long countByStatusIn(List<ComplaintStatus> statuses);
}
