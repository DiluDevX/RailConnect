package lk.sliit.railconnect.features.complaint;

import lk.sliit.railconnect.features.complaint.domain.ComplaintEvent;
import lk.sliit.railconnect.features.complaint.domain.ComplaintEventType;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintEventTest {
    @Test
    void eventCarriesTheActiveComplaintModulesTypes() {
        ComplaintEvent event = new ComplaintEvent(1L, "CMP-TEST", 2L,
                ComplaintEventType.SUBMITTED, ComplaintStatus.OPEN, "Ticket issue");

        assertThat(event.complaintId()).isEqualTo(1L);
        assertThat(event.complaintReference()).isEqualTo("CMP-TEST");
        assertThat(event.passengerId()).isEqualTo(2L);
        assertThat(event.eventType()).isEqualTo(ComplaintEventType.SUBMITTED);
        assertThat(event.status()).isEqualTo(ComplaintStatus.OPEN);
        assertThat(event.subject()).isEqualTo("Ticket issue");
        assertThat(ComplaintEventType.values()).containsExactly(
                ComplaintEventType.SUBMITTED, ComplaintEventType.UPDATED,
                ComplaintEventType.WITHDRAWN, ComplaintEventType.RESPONDED);
    }
}
