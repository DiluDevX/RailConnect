package com.train.scheduling.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "carriages")
public class Carriage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long carriageId;

    @Column(nullable = false)
    private Long trainId; // Mock reference since Train module is separate

    @Column(nullable = false, unique = true)
    private String carriageNumber;

    @Column(nullable = false)
    private String classType; // e.g., First Class, Second Class, Third Class

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String status; // Active, Inactive

    public Carriage() {}

    public Carriage(Long carriageId, Long trainId, String carriageNumber, String classType, Integer capacity, String status) {
        this.carriageId = carriageId;
        this.trainId = trainId;
        this.carriageNumber = carriageNumber;
        this.classType = classType;
        this.capacity = capacity;
        this.status = status;
    }

    public Long getCarriageId() { return carriageId; }
    public void setCarriageId(Long carriageId) { this.carriageId = carriageId; }
    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public String getCarriageNumber() { return carriageNumber; }
    public void setCarriageNumber(String carriageNumber) { this.carriageNumber = carriageNumber; }
    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static CarriageBuilder builder() {
        return new CarriageBuilder();
    }

    public static class CarriageBuilder {
        private Long carriageId;
        private Long trainId;
        private String carriageNumber;
        private String classType;
        private Integer capacity;
        private String status;

        public CarriageBuilder carriageId(Long carriageId) { this.carriageId = carriageId; return this; }
        public CarriageBuilder trainId(Long trainId) { this.trainId = trainId; return this; }
        public CarriageBuilder carriageNumber(String carriageNumber) { this.carriageNumber = carriageNumber; return this; }
        public CarriageBuilder classType(String classType) { this.classType = classType; return this; }
        public CarriageBuilder capacity(Integer capacity) { this.capacity = capacity; return this; }
        public CarriageBuilder status(String status) { this.status = status; return this; }

        public Carriage build() {
            return new Carriage(carriageId, trainId, carriageNumber, classType, capacity, status);
        }
    }
}
