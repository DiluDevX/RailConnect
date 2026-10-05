package lk.sliit.railconnect.features.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class BookingForm {
    @NotEmpty(message = "Select at least one seat")
    private List<Long> seatIds = new ArrayList<>();
    @Size(max = 120)
    private String passengerName;
    @Size(max = 160)
    private String contactEmail;
    @Size(max = 30)
    private String contactPhone;

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
