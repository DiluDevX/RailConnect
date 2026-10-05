package lk.sliit.railconnect.features.carriage.service;

import lk.sliit.railconnect.features.booking.repository.BookingSeatRepository;
import lk.sliit.railconnect.features.carriage.domain.Carriage;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.carriage.domain.Seat;
import lk.sliit.railconnect.features.carriage.domain.SeatStatus;
import lk.sliit.railconnect.features.carriage.domain.SeatType;
import lk.sliit.railconnect.features.carriage.dto.CarriageForm;
import lk.sliit.railconnect.features.carriage.repository.CarriageRepository;
import lk.sliit.railconnect.features.carriage.repository.SeatRepository;
import lk.sliit.railconnect.features.train.domain.Train;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarriageService {
    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final TrainService trainService;

    public CarriageService(CarriageRepository carriageRepository, SeatRepository seatRepository,
                           BookingSeatRepository bookingSeatRepository, TrainService trainService) {
        this.carriageRepository = carriageRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.trainService = trainService;
    }

    @Transactional(readOnly = true)
    public List<Carriage> list() { return carriageRepository.findAllByOrderByTrainTrainNumberAscCarriageNumberAsc(); }

    @Transactional(readOnly = true)
    public List<Carriage> forTrain(Long trainId) { return carriageRepository.findByTrainIdOrderByCarriageNumber(trainId); }

    @Transactional(readOnly = true)
    public Carriage require(Long id) {
        return carriageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Carriage was not found."));
    }

    @Transactional(readOnly = true)
    public List<Seat> seats(Long carriageId) { return seatRepository.findByCarriageIdOrderBySeatNumber(carriageId); }

    @Transactional(readOnly = true)
    public List<Seat> seatsForTrain(Long trainId) { return seatRepository.findByCarriageTrainIdOrderByCarriageCarriageNumberAscSeatNumberAsc(trainId); }

    @Transactional
    public Carriage create(CarriageForm form) {
        if (carriageRepository.existsByTrainIdAndCarriageNumberIgnoreCase(form.getTrainId(), form.getCarriageNumber().trim())) {
            throw new BusinessRuleException("Carriage number already exists for this train.");
        }
        Train train = trainService.require(form.getTrainId());
        Carriage carriage = carriageRepository.save(new Carriage(train, form.getCarriageNumber().trim(), form.getClassType(), form.getCapacity()));
        List<Seat> seats = new ArrayList<>();
        for (int number = 1; number <= form.getCapacity(); number++) {
            SeatType type = number % 4 == 1 || number % 4 == 0 ? SeatType.WINDOW : SeatType.AISLE;
            seats.add(new Seat(carriage, String.format("%02d", number), type));
        }
        seatRepository.saveAll(seats);
        return carriage;
    }

    @Transactional
    public Carriage update(Long id, CarriageForm form) {
        Carriage carriage = require(id);
        if (!carriage.getTrain().getId().equals(form.getTrainId())) {
            throw new BusinessRuleException("A carriage cannot be moved to another train after creation.");
        }
        if (form.getCapacity() != carriage.getCapacity()) {
            throw new BusinessRuleException("Capacity cannot be changed after seats are generated. Create a replacement carriage instead.");
        }
        if (carriageRepository.existsByTrainIdAndCarriageNumberIgnoreCaseAndIdNot(form.getTrainId(), form.getCarriageNumber().trim(), id)) {
            throw new BusinessRuleException("Carriage number already exists for this train.");
        }
        carriage.update(form.getCarriageNumber().trim(), form.getClassType(), form.getStatus());
        return carriage;
    }

    @Transactional
    public void toggleSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new ResourceNotFoundException("Seat was not found."));
        if (seat.getStatus() == SeatStatus.ACTIVE && bookingSeatRepository.existsBySeatId(seatId)) {
            throw new BusinessRuleException("A referenced seat cannot be placed out of service without reviewing its bookings.");
        }
        seat.setStatus(seat.getStatus() == SeatStatus.ACTIVE ? SeatStatus.OUT_OF_SERVICE : SeatStatus.ACTIVE);
    }

    @Transactional
    public void toggleCarriage(Long id) {
        Carriage carriage = require(id);
        CarriageStatus next = carriage.getStatus() == CarriageStatus.INACTIVE ? CarriageStatus.ACTIVE : CarriageStatus.INACTIVE;
        carriage.update(carriage.getCarriageNumber(), carriage.getClassType(), next);
    }
}
