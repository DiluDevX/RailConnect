package lk.sliit.railconnect.features.booking.dto;

import lk.sliit.railconnect.features.carriage.domain.Seat;

public record SeatOption(Seat seat, boolean available) {
}
