package com.railway.complaintmanagement;

import com.railway.complaintmanagement.model.Complaint;
import com.railway.complaintmanagement.model.ComplaintType;

public class ComplaintForm {
    private Long bookingId;
    private ComplaintType complaintType;
    private String subject;
    private String description;

    public static ComplaintForm from(Complaint complaint) {
        ComplaintForm form = new ComplaintForm();
        form.bookingId = complaint.getBookingId();
        form.complaintType = complaint.getComplaintType();
        form.subject = complaint.getSubject();
        form.description = complaint.getDescription();
        return form;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public ComplaintType getComplaintType() { return complaintType; }
    public void setComplaintType(ComplaintType complaintType) { this.complaintType = complaintType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
