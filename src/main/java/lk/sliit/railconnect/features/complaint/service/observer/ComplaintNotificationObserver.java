package lk.sliit.railconnect.features.complaint.service.observer;

import lk.sliit.railconnect.features.complaint.domain.ComplaintEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ComplaintNotificationObserver implements ComplaintObserver {

    private static final Logger log =
            LoggerFactory.getLogger(ComplaintNotificationObserver.class);

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void update(ComplaintEvent event) {
        switch (event.eventType()) {
            case SUBMITTED ->
                    log.info("Notification: complaint {} was submitted",
                            event.complaintReference());

            case UPDATED ->
                    log.info("Notification: complaint {} was updated",
                            event.complaintReference());

            case WITHDRAWN ->
                    log.info("Notification: complaint {} was withdrawn",
                            event.complaintReference());

            case RESPONDED ->
                    log.info("Notification: admin responded to complaint {}",
                            event.complaintReference());
        }
    }
}
