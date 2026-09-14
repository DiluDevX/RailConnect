package lk.sliit.railconnect.features.train.service;

import lk.sliit.railconnect.features.carriage.repository.CarriageRepository;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.TrainForm;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainService {
    private final TrainRepository trainRepository;
    private final CarriageRepository carriageRepository;

    public TrainService(TrainRepository trainRepository, CarriageRepository carriageRepository) {
        this.trainRepository = trainRepository;
        this.carriageRepository = carriageRepository;
    }

    @Transactional(readOnly = true)
    public List<Train> list(String query) {
        if (query == null || query.isBlank()) {
            return trainRepository.findAllByOrderByTrainNumber();
        }
        return trainRepository.findByTrainNumberContainingIgnoreCaseOrTrainNameContainingIgnoreCaseOrderByTrainNumber(query, query);
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
        if (trainRepository.existsByTrainNumberIgnoreCase(form.getTrainNumber().trim())) {
            throw new BusinessRuleException("Train number already exists.");
        }
        Train train = new Train(form.getTrainNumber().trim(), form.getTrainName().trim(), clean(form.getDescription()));
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), form.getFirstClassFare(),
                form.getSecondClassFare(), form.getThirdClassFare(), TrainStatus.ACTIVE);
        return trainRepository.save(train);
    }

    @Transactional
    public Train update(Long id, TrainForm form) {
        Train train = require(id);
        if (trainRepository.existsByTrainNumberIgnoreCaseAndIdNot(form.getTrainNumber().trim(), id)) {
            throw new BusinessRuleException("Train number already exists.");
        }
        train.update(form.getTrainNumber().trim(), form.getTrainName().trim(), clean(form.getDescription()),
                form.getFirstClassFare(), form.getSecondClassFare(), form.getThirdClassFare(), form.getStatus());
        return train;
    }

    @Transactional
    public void toggleActive(Long id) {
        Train train = require(id);
        TrainStatus next = train.getStatus() == TrainStatus.INACTIVE ? TrainStatus.ACTIVE : TrainStatus.INACTIVE;
        train.update(train.getTrainNumber(), train.getTrainName(), train.getDescription(), train.getFirstClassFare(),
                train.getSecondClassFare(), train.getThirdClassFare(), next);
    }

    @Transactional(readOnly = true)
    public long activeCapacity(Long trainId) {
        return carriageRepository.capacityForTrainAndStatus(trainId, CarriageStatus.ACTIVE);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
