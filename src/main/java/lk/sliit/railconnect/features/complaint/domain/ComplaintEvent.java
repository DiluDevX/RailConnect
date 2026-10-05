package lk.sliit.railconnect.features.complaint.domain;

public record ComplaintEvent(
        Long complaintId,
        String complaintReference,
        Long passengerId,
        ComplaintEventType eventType,
        ComplaintStatus status,
        String subject
) {
}
