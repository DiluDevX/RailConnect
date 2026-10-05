package lk.sliit.railconnect.features.complaint.service;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.service.BookingService;
import lk.sliit.railconnect.features.complaint.domain.Complaint;
import lk.sliit.railconnect.features.complaint.domain.ComplaintEvent;
import lk.sliit.railconnect.features.complaint.domain.ComplaintEventType;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.dto.ComplaintForm;
import lk.sliit.railconnect.features.complaint.repository.ComplaintRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final BookingService bookingService;
    private final ApplicationEventPublisher eventPublisher;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            BookingService bookingService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.complaintRepository = complaintRepository;
        this.bookingService = bookingService;
        this.eventPublisher = eventPublisher;
    }

    // Create a new complaint
    @Transactional
    public Complaint create(User user, ComplaintForm form) {

        TicketBooking booking = form.getBookingId() == null
                ? null
                : bookingService.requireAccessible(form.getBookingId(), user);

        Complaint complaint = new Complaint(
                newReference(),
                user,
                booking,
                form.getComplaintType(),
                form.getSubject().trim(),
                form.getDescription().trim()
        );

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Notify observers that a complaint was submitted
        publishComplaintEvent(
                savedComplaint,
                ComplaintEventType.SUBMITTED
        );

        return savedComplaint;
    }

    // Update an existing complaint
    @Transactional
    public Complaint updateByPassenger(
            Long id,
            User user,
            ComplaintForm form
    ) {

        Complaint complaint = requireAccessible(id, user);

        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new BusinessRuleException(
                    "Only open complaints can be edited."
            );
        }

        complaint.updateByPassenger(
                form.getComplaintType(),
                form.getSubject().trim(),
                form.getDescription().trim()
        );

        // Notify observers that a complaint was updated
        publishComplaintEvent(
                complaint,
                ComplaintEventType.UPDATED
        );

        return complaint;
    }

    // Withdraw a complaint
    @Transactional
    public void withdraw(Long id, User user) {

        Complaint complaint = requireAccessible(id, user);

        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new BusinessRuleException(
                    "Only open complaints can be withdrawn."
            );
        }

        complaint.withdraw();

        // Notify observers that a complaint was withdrawn
        publishComplaintEvent(
                complaint,
                ComplaintEventType.WITHDRAWN
        );
    }

    // Admin responds to a complaint
    @Transactional
    public void respond(
            Long id,
            ComplaintStatus status,
            String response
    ) {

        Complaint complaint = require(id);

        if (response == null || response.isBlank()) {
            throw new BusinessRuleException(
                    "Enter a response before updating the complaint."
            );
        }

        complaint.respond(status, response.trim());

        // Notify observers that an admin responded
        publishComplaintEvent(
                complaint,
                ComplaintEventType.RESPONDED
        );
    }

    // Publish a complaint event to all registered observers
    private void publishComplaintEvent(
            Complaint complaint,
            ComplaintEventType eventType
    ) {

        ComplaintEvent event = new ComplaintEvent(
                complaint.getId(),
                complaint.getComplaintReference(),
                complaint.getUser().getId(),
                eventType,
                complaint.getStatus(),
                complaint.getSubject()
        );

        eventPublisher.publishEvent(event);
    }

    // Find a complaint by ID
    @Transactional(readOnly = true)
    public Complaint require(Long id) {

        return complaintRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Complaint was not found."
                        )
                );
    }

    // Check whether the user can access the complaint
    @Transactional(readOnly = true)
    public Complaint requireAccessible(Long id, User user) {

        Complaint complaint = require(id);

        if (user.getRole() == UserRole.PASSENGER
                && !complaint.getUser().getId().equals(user.getId())) {

            throw new ResourceNotFoundException(
                    "Complaint was not found."
            );
        }

        return complaint;
    }

    // Get complaints belonging to a specific user
    @Transactional(readOnly = true)
    public List<Complaint> forUser(User user) {

        return complaintRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // Get all complaints
    @Transactional(readOnly = true)
    public List<Complaint> all() {

        return complaintRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // Generate a unique complaint reference
    private String newReference() {

        return "CMP-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }
}