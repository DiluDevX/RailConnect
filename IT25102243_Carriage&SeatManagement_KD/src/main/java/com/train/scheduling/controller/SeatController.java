package com.train.scheduling.controller;

import com.train.scheduling.entity.Seat;
import com.train.scheduling.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:5173") // Vite default port
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/carriage/{carriageId}")
    public ResponseEntity<List<Seat>> getSeatsByCarriageId(@PathVariable Long carriageId) {
        return ResponseEntity.ok(seatService.getSeatsByCarriageId(carriageId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Seat> updateSeatStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(seatService.updateSeatStatus(id, body.get("status")));
    }
    
    @PutMapping("/{id}/type")
    public ResponseEntity<Seat> updateSeatType(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(seatService.updateSeatType(id, body.get("seatType")));
    }
}
