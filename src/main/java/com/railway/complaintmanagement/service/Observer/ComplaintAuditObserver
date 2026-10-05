package lk.sliit.railconnect.features.complaint.service.observer;

import lk.sliit.railconnect.features.complaint.domain.ComplaintEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ComplaintAuditObserver implements ComplaintObserver {

    private static final Logger log =
            LoggerFactory.getLogger(ComplaintAuditObserver.class);

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void update(ComplaintEvent event) {
        log.info(
                "Complaint audit: reference={}, event={}, status={}",
                event.complaintReference(),
                event.eventType(),
                event.status()
        );
    }
}
