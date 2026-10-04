package com.railway.complaintmanagement.controller;

import com.railway.complaintmanagement.model.Complaint;
import com.railway.complaintmanagement.model.ComplaintStatus;
import com.railway.complaintmanagement.model.ComplaintType;
import com.railway.complaintmanagement.service.ComplaintService;
import com.railway.complaintmanagement.service.ComplaintServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplaintController {
    private final ComplaintService complaintService;

    public ComplaintController() {
        this(new ComplaintServiceImpl());
    }

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    public Complaint createComplaint(String userId, Long bookingId, ComplaintType complaintType, String subject, String description) {
        return complaintService.createComplaint(userId, bookingId, complaintType, subject, description);
    }

    public Complaint getComplaintById(String complaintId) {
        return complaintService.getComplaintById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint ID " + complaintId + " not found."));
    }

    public List<Complaint> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    public List<Complaint> getComplaintsByUser(String userId) {
        return complaintService.getComplaintsByUser(userId);
    }

    public Map<String, Object> updateComplaint(String complaintId, ComplaintType complaintType, String subject, String description) {
        boolean success = complaintService.updateComplaint(complaintId, complaintType, subject, description);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Complaint updated successfully." : "Complaint not found.");
        return response;
    }

    public Map<String, Object> respondToComplaint(String complaintId, ComplaintStatus status, String response) {
        boolean success = complaintService.respondToComplaint(complaintId, status.name(), response);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Complaint response saved." : "Complaint not found.");
        return result;
    }

    public Map<String, Object> withdrawComplaint(String complaintId) {
        boolean success = complaintService.withdrawComplaint(complaintId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Complaint withdrawn." : "Complaint not found.");
        return response;
    }
}
