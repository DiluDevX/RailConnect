package com.train.scheduling.repository;

import com.train.scheduling.entity.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarriageRepository extends JpaRepository<Carriage, Long> {
    List<Carriage> findByTrainId(Long trainId);
    List<Carriage> findByStatus(String status);
}
