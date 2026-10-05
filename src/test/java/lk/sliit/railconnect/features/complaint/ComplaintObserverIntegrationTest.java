package lk.sliit.railconnect.features.complaint;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.repository.UserRepository;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.domain.ComplaintType;
import lk.sliit.railconnect.features.complaint.dto.ComplaintForm;
import lk.sliit.railconnect.features.complaint.service.ComplaintService;
import lk.sliit.railconnect.features.complaint.service.observer.ComplaintAuditObserver;
import lk.sliit.railconnect.features.complaint.service.observer.ComplaintNotificationObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ComplaintObserverIntegrationTest {
    @Autowired ComplaintService service;
    @Autowired UserRepository users;
    @Autowired PlatformTransactionManager transactionManager;
    private final ListAppender<ILoggingEvent> audit = new ListAppender<>();
    private final ListAppender<ILoggingEvent> notifications = new ListAppender<>();

    @BeforeEach
    void clearObserverCalls() {
        audit.list.clear();
        notifications.list.clear();
        audit.start();
        notifications.start();
        ((Logger) LoggerFactory.getLogger(ComplaintAuditObserver.class)).addAppender(audit);
        ((Logger) LoggerFactory.getLogger(ComplaintNotificationObserver.class)).addAppender(notifications);
    }

    @AfterEach
    void detachLogCapture() {
        ((Logger) LoggerFactory.getLogger(ComplaintAuditObserver.class)).detachAppender(audit);
        ((Logger) LoggerFactory.getLogger(ComplaintNotificationObserver.class)).detachAppender(notifications);
        audit.stop();
        notifications.stop();
    }

    @Test
    void bothObserversReceiveAllLifecycleEventsOnlyAfterCommit() {
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            User user = users.save(new User("Observer Tester", "observer@example.com", "encoded",
                    "0770000000", UserRole.PASSENGER));
            var withdrawn = service.create(user, form("Original"));
            service.updateByPassenger(withdrawn.getId(), user, form("Updated"));
            service.withdraw(withdrawn.getId(), user);
            var resolved = service.create(user, form("Another complaint"));
            service.respond(resolved.getId(), ComplaintStatus.RESOLVED, "Resolved by staff");
            assertThat(audit.list).isEmpty();
            assertThat(notifications.list).isEmpty();
        });
        assertThat(audit.list).hasSize(5);
        assertThat(notifications.list).hasSize(5);
        assertThat(audit.list.get(0).getFormattedMessage()).contains("event=SUBMITTED", "status=OPEN");
        assertThat(audit.list.get(1).getFormattedMessage()).contains("event=UPDATED", "status=OPEN");
        assertThat(audit.list.get(2).getFormattedMessage()).contains("event=WITHDRAWN", "status=WITHDRAWN");
        assertThat(audit.list.get(4).getFormattedMessage()).contains("event=RESPONDED", "status=RESOLVED");
    }

    @Test
    void rollbackDoesNotNotifyObservers() {
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            User user = users.save(new User("Rollback Tester", "observer-rollback@example.com", "encoded",
                    "0770000000", UserRole.PASSENGER));
            service.create(user, form("Must not notify"));
            transaction.setRollbackOnly();
        });
        assertThat(audit.list).isEmpty();
        assertThat(notifications.list).isEmpty();
        assertThat(users.findByEmailIgnoreCase("observer-rollback@example.com")).isEmpty();
    }

    private ComplaintForm form(String subject) {
        ComplaintForm form = new ComplaintForm();
        form.setComplaintType(ComplaintType.SERVICE);
        form.setSubject(subject);
        form.setDescription("Observer integration test");
        return form;
    }
}
