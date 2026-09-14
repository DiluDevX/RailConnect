package com.railconnect.schedule_service.service;

import com.railconnect.schedule_service.entity.Schedule;
import com.railconnect.schedule_service.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    // CREATE
    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    // READ - all
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    // READ - by id
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    // UPDATE
    public Schedule updateSchedule(Long id, Schedule updatedSchedule) {
        return scheduleRepository.findById(id)
                .map(existing -> {
                    existing.setTrainId(updatedSchedule.getTrainId());
                    existing.setRouteId(updatedSchedule.getRouteId());
                    existing.setTravelDate(updatedSchedule.getTravelDate());
                    existing.setDepartureTime(updatedSchedule.getDepartureTime());
                    existing.setArrivalTime(updatedSchedule.getArrivalTime());
                    existing.setBaseFare(updatedSchedule.getBaseFare());
                    existing.setStatus(updatedSchedule.getStatus());
                    return scheduleRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));//404-style error
    }

    // DELETE
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }
}