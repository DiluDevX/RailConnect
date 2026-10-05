package lk.sliit.railconnect.features.booking.service;

import lk.sliit.railconnect.features.booking.domain.TicketBooking;

public interface NotificationService {
    void bookingConfirmed(TicketBooking booking);
    void bookingCancelled(TicketBooking booking);
}
