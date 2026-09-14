package com.train.scheduling.service;

import com.train.scheduling.entity.Seat;
import com.train.scheduling.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getSeatsByCarriageId(Long carriageId) {
        return seatRepository.findByCarriageId(carriageId);
    }

    public Seat updateSeatStatus(Long seatId, String status) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        seat.setStatus(status);
        return seatRepository.save(seat);
    }
    
    public Seat updateSeatType(Long seatId, String seatType) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));
        seat.setSeatType(seatType);
        return seatRepository.save(seat);
    }
}
