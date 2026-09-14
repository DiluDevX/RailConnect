package com.railway.complaintmanagement.model;

import java.util.Objects;

public class Complaint {
    private String complaintId;
    private String userId;
    private Long bookingId;
    private ComplaintType complaintType;
    private String subject;
    private String description;
    private String status = ComplaintStatus.OPEN.name();
    private String adminResponse;

    public Complaint() {
    }

    public Complaint(String complaintId, String userId, Long bookingId,
                    ComplaintType complaintType, String subject, String description) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.bookingId = bookingId;
        this.complaintType = complaintType;
        this.subject = subject;
        this.description = description;
    }

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public ComplaintType getComplaintType() { return complaintType; }
    public void setComplaintType(ComplaintType complaintType) { this.complaintType = complaintType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public void updateByPassenger(ComplaintType type, String subject, String description) {
        this.complaintType = type;
        this.subject = subject;
        this.description = description;
    }

    public void respond(String status, String response) {
        this.status = status;
        this.adminResponse = response;
    }

    public void withdraw() {
        this.status = ComplaintStatus.WITHDRAWN.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(complaintId, complaint.complaintId);
    }

    @Override
    public int hashCode() { return Objects.hash(complaintId); }

    @Override
    public String toString() {
        return "Complaint{" +
                "complaintId='" + complaintId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookingId=" + bookingId +
                ", complaintType=" + complaintType +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
