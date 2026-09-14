package com.train.scheduling.controller;

import com.train.scheduling.entity.Carriage;
import com.train.scheduling.service.CarriageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriages")
@CrossOrigin(origins = "http://localhost:5173") // Vite default port
public class CarriageController {

    private final CarriageService carriageService;

    public CarriageController(CarriageService carriageService) {
        this.carriageService = carriageService;
    }

    @PostMapping
    public ResponseEntity<Carriage> createCarriage(@RequestBody Carriage carriage) {
        Carriage created = carriageService.createCarriage(carriage);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Carriage>> getAllCarriages() {
        return ResponseEntity.ok(carriageService.getAllCarriages());
    }
    
    @GetMapping("/train/{trainId}")
    public ResponseEntity<List<Carriage>> getCarriagesByTrainId(@PathVariable Long trainId) {
        return ResponseEntity.ok(carriageService.getCarriagesByTrainId(trainId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carriage> getCarriageById(@PathVariable Long id) {
        return ResponseEntity.ok(carriageService.getCarriageById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carriage> updateCarriage(@PathVariable Long id, @RequestBody Carriage carriage) {
        return ResponseEntity.ok(carriageService.updateCarriage(id, carriage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCarriage(@PathVariable Long id) {
        carriageService.deactivateCarriage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateCarriage(@PathVariable Long id) {
        carriageService.reactivateCarriage(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteCarriage(@PathVariable Long id) {
        carriageService.hardDeleteCarriage(id);
        return ResponseEntity.noContent().build();
    }
}
