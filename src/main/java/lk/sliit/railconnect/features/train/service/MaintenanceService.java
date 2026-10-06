package lk.sliit.railconnect.features.train.service;

import lk.sliit.railconnect.features.train.domain.Maintenance;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.dto.MaintenanceForm;
import lk.sliit.railconnect.features.train.repository.MaintenanceRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final TrainService trainService;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, TrainService trainService) {
        this.maintenanceRepository = maintenanceRepository;
        this.trainService = trainService;
    }

    public List<Maintenance> search(Long trainId, MaintenanceStatus status, String type, LocalDate date) {
        String keyword = type == null || type.isBlank() ? null : type.trim();
        return maintenanceRepository.search(trainId, status, keyword, date);
    }

    public Maintenance require(Long id) {
        return maintenanceRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record was not found."));
    }

    public List<Maintenance> historyForTrain(Long trainId) {
        trainService.require(trainId);
        return maintenanceRepository.findHistoryByTrainId(trainId);
    }

    @Transactional
    public Maintenance create(MaintenanceForm form) {
        Train train = trainService.require(form.getTrainId());
        Maintenance maintenance = new Maintenance(form.getMaintenanceType().trim(), train, form.getMaintenanceDate(),
                clean(form.getDescription()), form.getStatus());
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public Maintenance update(Long id, MaintenanceForm form) {
        Maintenance maintenance = require(id);
        Train train = trainService.require(form.getTrainId());
        maintenance.update(form.getMaintenanceType().trim(), train, form.getMaintenanceDate(),
                clean(form.getDescription()), form.getStatus());
        return maintenance;
    }

    @Transactional
    public void cancel(Long id) {
        Maintenance maintenance = require(id);
        if (maintenance.getStatus() == MaintenanceStatus.COMPLETED) {
            throw new BusinessRuleException("Completed maintenance cannot be cancelled.");
        }
        if (maintenance.getStatus() == MaintenanceStatus.CANCELLED) {
            throw new BusinessRuleException("Maintenance is already cancelled.");
        }
        maintenance.cancel();
    }

    @Transactional
    public void deleteScheduled(Long id) {
        Maintenance maintenance = require(id);
        if (maintenance.getStatus() != MaintenanceStatus.SCHEDULED) {
            throw new BusinessRuleException("Only SCHEDULED maintenance can be permanently deleted.");
        }
        maintenanceRepository.delete(maintenance);
    }

    public long countAll() {
        return maintenanceRepository.count();
    }

    public long countByStatus(MaintenanceStatus status) {
        return maintenanceRepository.countByStatus(status);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
