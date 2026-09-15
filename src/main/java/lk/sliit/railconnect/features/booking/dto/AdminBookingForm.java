package lk.sliit.railconnect.features.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminBookingForm {
    @NotBlank(message = "Passenger name is required")
    @Size(max = 120)
    private String passengerName;

    @Size(max = 160)
    private String contactEmail;

    @Size(max = 30)
    private String contactPhone;

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
