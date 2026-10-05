package lk.sliit.railconnect.features.booking.dto;

import lk.sliit.railconnect.features.carriage.domain.Carriage;

import java.math.BigDecimal;
import java.util.List;

public record CarriageSeatGroup(Carriage carriage, List<SeatOption> seats, BigDecimal farePerSeat) {
}
