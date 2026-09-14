package com.train.scheduling.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @Column(nullable = false)
    private Long carriageId;

    @Column(nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private String seatType; // Window, Aisle, Middle

    @Column(nullable = false)
    private String status; // Active, Inactive

    public Seat() {}

    public Seat(Long seatId, Long carriageId, String seatNumber, String seatType, String status) {
        this.seatId = seatId;
        this.carriageId = carriageId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
    }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public Long getCarriageId() { return carriageId; }
    public void setCarriageId(Long carriageId) { this.carriageId = carriageId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static SeatBuilder builder() {
        return new SeatBuilder();
    }

    public static class SeatBuilder {
        private Long seatId;
        private Long carriageId;
        private String seatNumber;
        private String seatType;
        private String status;

        public SeatBuilder seatId(Long seatId) { this.seatId = seatId; return this; }
        public SeatBuilder carriageId(Long carriageId) { this.carriageId = carriageId; return this; }
        public SeatBuilder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public SeatBuilder seatType(String seatType) { this.seatType = seatType; return this; }
        public SeatBuilder status(String status) { this.status = status; return this; }

        public Seat build() {
            return new Seat(seatId, carriageId, seatNumber, seatType, status);
        }
    }
}
