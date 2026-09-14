package lk.sliit.railconnect.auth.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.auth.dto.RegistrationForm;
import lk.sliit.railconnect.auth.dto.ProfileForm;
import lk.sliit.railconnect.auth.dto.ChangePasswordForm;
import lk.sliit.railconnect.auth.dto.ResetPasswordForm;
import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.service.UserService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm registrationForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerPassenger(registrationForm);
        } catch (BusinessRuleException exception) {
            bindingResult.reject("registration", exception.getMessage());
            return "auth/register";
        }
        redirectAttributes.addFlashAttribute("success", "Account created. You can now sign in.");
        return "redirect:/login";
    }

    @GetMapping("/account")
    public String account(Authentication authentication, Model model) {
        User user = userService.byEmail(authentication.getName());
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", ProfileForm.from(user));
        }
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        model.addAttribute("user", user);
        return "auth/account";
    }

    @PostMapping("/account")
    public String updateAccount(@Valid @ModelAttribute ProfileForm profileForm,
                                BindingResult bindingResult, Authentication authentication,
                                Model model, RedirectAttributes redirectAttributes) {
        User user = userService.byEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            return "auth/account";
        }
        userService.updateProfile(authentication.getName(), profileForm);
        redirectAttributes.addFlashAttribute("success", "Account details updated.");
        return "redirect:/account";
    }

    @PostMapping("/account/password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordForm changePasswordForm,
                                 BindingResult bindingResult, Authentication authentication,
                                 Model model, RedirectAttributes redirectAttributes) {
        User user = userService.byEmail(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileForm", ProfileForm.from(user));
            model.addAttribute("changePasswordForm", changePasswordForm);
            return "auth/account";
        }
        try {
            userService.changePassword(authentication.getName(), changePasswordForm);
        } catch (BusinessRuleException exception) {
            bindingResult.reject("password", exception.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("profileForm", ProfileForm.from(user));
            model.addAttribute("changePasswordForm", changePasswordForm);
            return "auth/account";
        }
        redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/account";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        if (!model.containsAttribute("email")) model.addAttribute("email", "");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@ModelAttribute("email") String email, RedirectAttributes redirectAttributes) {
        String token = userService.requestPasswordReset(email == null ? "" : email);
        redirectAttributes.addFlashAttribute("success", "If an account exists for that email, reset instructions are ready.");
        if (token != null) {
            redirectAttributes.addFlashAttribute("demoResetLink", "/reset-password?token=" + token);
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@org.springframework.web.bind.annotation.RequestParam String token, Model model) {
        model.addAttribute("token", token);
        if (!model.containsAttribute("resetPasswordForm")) model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@org.springframework.web.bind.annotation.RequestParam String token,
                                @Valid @ModelAttribute ResetPasswordForm resetPasswordForm,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        try {
            userService.resetPassword(token, resetPasswordForm);
        } catch (BusinessRuleException exception) {
            bindingResult.reject("reset", exception.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        redirectAttributes.addFlashAttribute("success", "Password reset. You can now sign in.");
        return "redirect:/login";
    }
}
