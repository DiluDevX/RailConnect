package lk.sliit.railconnect.features.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.features.complaint.domain.Complaint;
import lk.sliit.railconnect.features.complaint.domain.ComplaintType;

public class ComplaintForm {
    private Long bookingId;
    @NotNull private ComplaintType complaintType;
    @NotBlank @Size(max = 160) private String subject;
    @NotBlank @Size(max = 2000) private String description;

    public static ComplaintForm from(Complaint complaint) {
        ComplaintForm form = new ComplaintForm();
        form.bookingId = complaint.getBooking() == null ? null : complaint.getBooking().getId();
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
