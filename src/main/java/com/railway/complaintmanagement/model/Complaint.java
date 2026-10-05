package lk.sliit.railconnect.features.complaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.shared.domain.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "complaints")
public class Complaint extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String complaintReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private TicketBooking booking;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ComplaintType complaintType;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(length = 2000)
    private String adminResponse;

    protected Complaint() {
    }

    public Complaint(String complaintReference, User user, TicketBooking booking, ComplaintType complaintType,
                     String subject, String description) {
        this.complaintReference = complaintReference;
        this.user = user;
        this.booking = booking;
        this.complaintType = complaintType;
        this.subject = subject;
        this.description = description;
    }

    public String getComplaintReference() { return complaintReference; }
    public User getUser() { return user; }
    public TicketBooking getBooking() { return booking; }
    public ComplaintType getComplaintType() { return complaintType; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public ComplaintStatus getStatus() { return status; }
    public String getAdminResponse() { return adminResponse; }

    public void updateByPassenger(ComplaintType type, String subject, String description) {
        this.complaintType = type;
        this.subject = subject;
        this.description = description;
    }

    public void respond(ComplaintStatus status, String response) {
        this.status = status;
        this.adminResponse = response;
    }

    public void withdraw() {
        this.status = ComplaintStatus.WITHDRAWN;
    }
}
