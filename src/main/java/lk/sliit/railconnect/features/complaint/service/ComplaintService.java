package lk.sliit.railconnect.features.complaint.service;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.service.BookingService;
import lk.sliit.railconnect.features.complaint.domain.Complaint;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.dto.ComplaintForm;
import lk.sliit.railconnect.features.complaint.repository.ComplaintRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final BookingService bookingService;

    public ComplaintService(ComplaintRepository complaintRepository, BookingService bookingService) {
        this.complaintRepository = complaintRepository;
        this.bookingService = bookingService;
    }

    @Transactional
    public Complaint create(User user, ComplaintForm form) {
        TicketBooking booking = form.getBookingId() == null ? null : bookingService.requireAccessible(form.getBookingId(), user);
        Complaint complaint = new Complaint(newReference(), user, booking, form.getComplaintType(),
                form.getSubject().trim(), form.getDescription().trim());
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint updateByPassenger(Long id, User user, ComplaintForm form) {
        Complaint complaint = requireAccessible(id, user);
        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new BusinessRuleException("Only open complaints can be edited.");
        }
        complaint.updateByPassenger(form.getComplaintType(), form.getSubject().trim(), form.getDescription().trim());
        return complaint;
    }

    @Transactional
    public void withdraw(Long id, User user) {
        Complaint complaint = requireAccessible(id, user);
        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new BusinessRuleException("Only open complaints can be withdrawn.");
        }
        complaint.withdraw();
    }

    @Transactional
    public void respond(Long id, ComplaintStatus status, String response) {
        Complaint complaint = require(id);
        if (response == null || response.isBlank()) {
            throw new BusinessRuleException("Enter a response before updating the complaint.");
        }
        complaint.respond(status, response.trim());
    }

    @Transactional(readOnly = true)
    public Complaint require(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint was not found."));
    }

    @Transactional(readOnly = true)
    public Complaint requireAccessible(Long id, User user) {
        Complaint complaint = require(id);
        if (user.getRole() == UserRole.PASSENGER && !complaint.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Complaint was not found.");
        }
        return complaint;
    }

    @Transactional(readOnly = true)
    public List<Complaint> forUser(User user) { return complaintRepository.findByUserIdOrderByCreatedAtDesc(user.getId()); }

    @Transactional(readOnly = true)
    public List<Complaint> all() { return complaintRepository.findAllByOrderByCreatedAtDesc(); }

    private String newReference() {
        return "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
