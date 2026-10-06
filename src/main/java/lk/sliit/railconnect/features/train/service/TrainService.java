package lk.sliit.railconnect.features.train.service;

import lk.sliit.railconnect.features.carriage.repository.CarriageRepository;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.schedule.repository.TrainScheduleRepository;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.TrainForm;
import lk.sliit.railconnect.features.train.repository.MaintenanceRepository;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class TrainService {
    private static final Pattern TRAIN_NUMBER = Pattern.compile("\\d{4}");

    private final TrainRepository trainRepository;
    private final CarriageRepository carriageRepository;
    private final TrainScheduleRepository scheduleRepository;
    private final MaintenanceRepository maintenanceRepository;

    public TrainService(TrainRepository trainRepository, CarriageRepository carriageRepository,
                        TrainScheduleRepository scheduleRepository, MaintenanceRepository maintenanceRepository) {
        this.trainRepository = trainRepository;
        this.carriageRepository = carriageRepository;
        this.scheduleRepository = scheduleRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional(readOnly = true)
    public List<Train> list(String query) {
        return search(query, null);
    }

    @Transactional(readOnly = true)
    public List<Train> search(String query, TrainStatus status) {
        String keyword = query == null || query.isBlank() ? null : query.trim();
        return trainRepository.search(keyword, status);
    }

    @Transactional(readOnly = true)
    public List<Train> active() {
        return trainRepository.findByStatusOrderByTrainNumber(TrainStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Train require(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train was not found."));
    }

    @Transactional
    public Train create(TrainForm form) {
        String trainNumber = requireTrainNumber(form.getTrainNumber(), null);
        Train train = new Train(trainNumber, form.getTrainName().trim(), clean(form.getDescription()));
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), TrainStatus.ACTIVE);
        return trainRepository.save(train);
    }

    @Transactional
    public Train update(Long id, TrainForm form) {
        Train train = require(id);
        String trainNumber = requireTrainNumber(form.getTrainNumber(), id);
        train.update(trainNumber, form.getTrainName().trim(), clean(form.getDescription()), form.getStatus());
        return train;
    }

    @Transactional
    public void toggleActive(Long id) {
        Train train = require(id);
        TrainStatus next = train.getStatus() == TrainStatus.INACTIVE ? TrainStatus.ACTIVE : TrainStatus.INACTIVE;
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), next);
    }

    @Transactional
    public void activate(Long id) {
        Train train = require(id);
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), TrainStatus.ACTIVE);
    }

    @Transactional
    public void deactivate(Long id) {
        Train train = require(id);
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), TrainStatus.INACTIVE);
    }

    @Transactional
    public void delete(Long id) {
        Train train = require(id);
        if (train.getStatus() != TrainStatus.INACTIVE) {
            throw new BusinessRuleException("Deactivate the train before deleting it.");
        }
        if (scheduleRepository.existsByTrain_Id(id)) {
            throw new BusinessRuleException("This train has schedule history and cannot be permanently deleted. Cancelling active schedules does not remove their history; keep this train inactive to preserve the linked schedule and booking records.");
        }
        if (carriageRepository.existsByTrain_Id(id) || maintenanceRepository.existsByTrain_Id(id)) {
            throw new BusinessRuleException("This train has carriage or maintenance history and cannot be permanently deleted while those records are retained.");
        }
        trainRepository.delete(train);
    }

    @Transactional(readOnly = true)
    public long activeCapacity(Long trainId) {
        return carriageRepository.capacityForTrainAndStatus(trainId, CarriageStatus.ACTIVE);
    }

    private String requireTrainNumber(String number, Long currentId) {
        if (number == null || number.isBlank()) {
            throw new BusinessRuleException("Train number is required.");
        }
        String normalized = number.trim();
        // Existing identifiers (including seeded DEMO-* trains) remain editable.
        // New or changed identifiers must follow the member's four-digit rule.
        boolean unchangedNumber = currentId != null && require(currentId).getTrainNumber().equals(normalized);
        if (!unchangedNumber && !TRAIN_NUMBER.matcher(normalized).matches()) {
            throw new BusinessRuleException("Train number must be exactly 4 digits, for example 1001.");
        }
        boolean alreadyUsed = currentId == null
                ? trainRepository.existsByTrainNumberIgnoreCase(normalized)
                : trainRepository.existsByTrainNumberIgnoreCaseAndIdNot(normalized, currentId);
        if (alreadyUsed) {
            throw new BusinessRuleException("Train number " + normalized + " is already used and cannot be repeated.");
        }
        return normalized;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
