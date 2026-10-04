package com.railway.complaintmanagement.dao;

import com.railway.complaintmanagement.model.Complaint;
import com.railway.complaintmanagement.model.ComplaintStatus;
import com.railway.complaintmanagement.model.ComplaintType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ComplaintDaoImpl implements ComplaintDao {
    private final Map<String, Complaint> complaints = new ConcurrentHashMap<>();

    public ComplaintDaoImpl() {
        seedData();
    }

    private void seedData() {
        Complaint c1 = new Complaint("CMP-1001", "USER-001", 1L, ComplaintType.BOOKING, "Ticket issue", "Booking confirmation was delayed");
        c1.setStatus(ComplaintStatus.OPEN.name());
        complaints.put(c1.getComplaintId(), c1);

        Complaint c2 = new Complaint("CMP-1002", "USER-002", 2L, ComplaintType.SERVICE, "Poor staff support", "Staff were not helpful at station");
        c2.setStatus(ComplaintStatus.IN_REVIEW.name());
        complaints.put(c2.getComplaintId(), c2);
    }

    @Override
    public Complaint save(Complaint complaint) {
        complaints.put(complaint.getComplaintId(), complaint);
        return complaint;
    }

    @Override
    public Optional<Complaint> findById(String complaintId) {
        return Optional.ofNullable(complaints.get(complaintId));
    }

    @Override
    public List<Complaint> findAll() {
        return new ArrayList<>(complaints.values());
    }

    @Override
    public List<Complaint> findByUserId(String userId) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint complaint : complaints.values()) {
            if (complaint.getUserId().equals(userId)) {
                result.add(complaint);
            }
        }
        return result;
    }

    @Override
    public List<Complaint> findByStatus(String status) {
        List<Complaint> result = new ArrayList<>();
        for (Complaint complaint : complaints.values()) {
            if (complaint.getStatus().equalsIgnoreCase(status)) {
                result.add(complaint);
            }
        }
        return result;
    }

    @Override
    public boolean update(Complaint complaint) {
        if (complaints.containsKey(complaint.getComplaintId())) {
            complaints.put(complaint.getComplaintId(), complaint);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(String complaintId) {
        return complaints.remove(complaintId) != null;
    }
}
