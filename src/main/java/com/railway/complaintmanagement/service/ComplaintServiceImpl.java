package com.railway.complaintmanagement.service;

import com.railway.complaintmanagement.dao.ComplaintDao;
import com.railway.complaintmanagement.dao.ComplaintDaoImpl;
import com.railway.complaintmanagement.model.Complaint;
import com.railway.complaintmanagement.model.ComplaintStatus;
import com.railway.complaintmanagement.model.ComplaintType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ComplaintServiceImpl implements ComplaintService {
    private final ComplaintDao complaintDao;

    public ComplaintServiceImpl() {
        this(new ComplaintDaoImpl());
    }

    public ComplaintServiceImpl(ComplaintDao complaintDao) {
        this.complaintDao = complaintDao;
    }

    @Override
    public Complaint createComplaint(String userId, Long bookingId, ComplaintType complaintType, String subject, String description) {
        validateComplaint(userId, complaintType, subject, description);

        String complaintId = "CMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Complaint complaint = new Complaint(complaintId, userId, bookingId, complaintType, subject.trim(), description.trim());
        complaint.setStatus(ComplaintStatus.OPEN.name());
        return complaintDao.save(complaint);
    }

    @Override
    public Optional<Complaint> getComplaintById(String complaintId) {
        if (complaintId == null || complaintId.trim().isEmpty()) return Optional.empty();
        return complaintDao.findById(complaintId.trim());
    }

    @Override
    public List<Complaint> getAllComplaints() {
        return complaintDao.findAll();
    }

    @Override
    public List<Complaint> getComplaintsByUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) return List.of();
        return complaintDao.findByUserId(userId.trim());
    }

    @Override
    public boolean updateComplaint(String complaintId, ComplaintType complaintType, String subject, String description) {
        Complaint complaint = complaintDao.findById(complaintId).orElse(null);
        if (complaint == null) return false;

        validateComplaint(complaint.getUserId(), complaintType, subject, description);
        complaint.updateByPassenger(complaintType, subject.trim(), description.trim());
        return complaintDao.update(complaint);
    }

    @Override
    public boolean respondToComplaint(String complaintId, String status, String response) {
        Complaint complaint = complaintDao.findById(complaintId).orElse(null);
        if (complaint == null) return false;

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }

        complaint.respond(status.trim().toUpperCase(), response == null ? "" : response.trim());
        return complaintDao.update(complaint);
    }

    @Override
    public boolean withdrawComplaint(String complaintId) {
        Complaint complaint = complaintDao.findById(complaintId).orElse(null);
        if (complaint == null) return false;
        complaint.withdraw();
        return complaintDao.update(complaint);
    }

    private void validateComplaint(String userId, ComplaintType complaintType, String subject, String description) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (complaintType == null) {
            throw new IllegalArgumentException("Complaint type is required.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required.");
        }
    }
}
