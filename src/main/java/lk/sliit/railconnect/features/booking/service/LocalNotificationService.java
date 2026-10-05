package lk.sliit.railconnect.features.booking.service;

import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalNotificationService.class);
    private final boolean enabled;

    public LocalNotificationService(@Value("${railconnect.notification.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void bookingConfirmed(TicketBooking booking) {
        if (enabled) {
            LOGGER.info("Simulated confirmation notification for booking {}", booking.getBookingReference());
        }
    }

    @Override
    public void bookingCancelled(TicketBooking booking) {
        if (enabled) {
            LOGGER.info("Simulated cancellation notification for booking {}", booking.getBookingReference());
        }
    }
}
