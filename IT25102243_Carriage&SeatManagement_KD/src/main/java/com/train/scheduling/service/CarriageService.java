package com.train.scheduling.service;

import com.train.scheduling.entity.Carriage;
import com.train.scheduling.entity.Seat;
import com.train.scheduling.repository.CarriageRepository;
import com.train.scheduling.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarriageService {

    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;

    public CarriageService(CarriageRepository carriageRepository, SeatRepository seatRepository) {
        this.carriageRepository = carriageRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Carriage createCarriage(Carriage carriage) {
        carriage.setStatus("Active");
        Carriage savedCarriage = carriageRepository.save(carriage);

        // Auto-generate seats
        List<Seat> seats = new ArrayList<>();
        int capacity = savedCarriage.getCapacity();
        for (int i = 1; i <= capacity; i++) {
            String seatType = determineSeatType(i, classTypeColumns(savedCarriage.getClassType()));
            Seat seat = Seat.builder()
                    .carriageId(savedCarriage.getCarriageId())
                    .seatNumber(savedCarriage.getCarriageNumber() + "-" + i)
                    .seatType(seatType)
                    .status("Active")
                    .build();
            seats.add(seat);
        }
        seatRepository.saveAll(seats);

        return savedCarriage;
    }

    public List<Carriage> getAllCarriages() {
        return carriageRepository.findAll();
    }
    
    public List<Carriage> getCarriagesByTrainId(Long trainId) {
        return carriageRepository.findByTrainId(trainId);
    }

    public Carriage getCarriageById(Long id) {
        return carriageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carriage not found"));
    }

    @Transactional
    public Carriage updateCarriage(Long id, Carriage updatedCarriage) {
        Carriage existing = getCarriageById(id);
        existing.setClassType(updatedCarriage.getClassType());
        // capacity changing would mean recreating seats, which is complex. 
        // For simplicity, we just update class and status.
        return carriageRepository.save(existing);
    }

    @Transactional
    public void deactivateCarriage(Long id) {
        Carriage existing = getCarriageById(id);
        existing.setStatus("Inactive");
        carriageRepository.save(existing);

        // Deactivate all associated seats
        List<Seat> seats = seatRepository.findByCarriageId(id);
        seats.forEach(seat -> seat.setStatus("Inactive"));
        seatRepository.saveAll(seats);
    }

    public void reactivateCarriage(Long id) {
        Carriage existing = getCarriageById(id);
        existing.setStatus("Active");
        carriageRepository.save(existing);

        // Reactivate all associated seats
        List<Seat> seats = seatRepository.findByCarriageId(id);
        seats.forEach(seat -> seat.setStatus("Active"));
        seatRepository.saveAll(seats);
    }

    public void hardDeleteCarriage(Long id) {
        // Delete all associated seats first
        List<Seat> seats = seatRepository.findByCarriageId(id);
        seatRepository.deleteAll(seats);
        // Delete carriage
        carriageRepository.deleteById(id);
    }

    private int classTypeColumns(String classType) {
        if ("First Class".equalsIgnoreCase(classType)) return 2;
        if ("Second Class".equalsIgnoreCase(classType)) return 3;
        return 4; // Default
    }

    private String determineSeatType(int seatNumber, int columnsPerRow) {
        int pos = (seatNumber - 1) % columnsPerRow;
        if (pos == 0 || pos == columnsPerRow - 1) {
            return "Window";
        }
        if (columnsPerRow == 2) {
            return "Aisle"; // In 2 col, both are window technically, but let's say Window/Aisle
        }
        if (pos == 1 && columnsPerRow > 2) {
             return (columnsPerRow == 3) ? "Aisle" : "Middle";
        }
        return "Aisle";
    }
}
