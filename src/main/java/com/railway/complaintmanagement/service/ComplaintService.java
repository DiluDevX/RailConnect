package com.railway.complaintmanagement.service;

import com.railway.complaintmanagement.model.Complaint;
import com.railway.complaintmanagement.model.ComplaintType;

import java.util.List;
import java.util.Optional;

public interface ComplaintService {
    Complaint createComplaint(String userId, Long bookingId, ComplaintType complaintType, String subject, String description);
    Optional<Complaint> getComplaintById(String complaintId);
    List<Complaint> getAllComplaints();
    List<Complaint> getComplaintsByUser(String userId);
    boolean updateComplaint(String complaintId, ComplaintType complaintType, String subject, String description);
    boolean respondToComplaint(String complaintId, String status, String response);
    boolean withdrawComplaint(String complaintId);
}
