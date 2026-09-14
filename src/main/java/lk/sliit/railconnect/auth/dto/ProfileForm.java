package lk.sliit.railconnect.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lk.sliit.railconnect.auth.domain.User;

public class ProfileForm {
    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^(?:\\+94|0)7\\d{8}$", message = "Enter a valid Sri Lankan mobile number")
    private String phone;

    public static ProfileForm from(User user) {
        ProfileForm form = new ProfileForm();
        form.setFullName(user.getFullName());
        form.setPhone(user.getPhone());
        return form;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
