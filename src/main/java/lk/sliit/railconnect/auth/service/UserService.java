package lk.sliit.railconnect.auth.service;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.domain.PasswordResetToken;
import lk.sliit.railconnect.auth.dto.ChangePasswordForm;
import lk.sliit.railconnect.auth.dto.RegistrationForm;
import lk.sliit.railconnect.auth.dto.ProfileForm;
import lk.sliit.railconnect.auth.dto.ResetPasswordForm;
import lk.sliit.railconnect.auth.repository.PasswordResetTokenRepository;
import lk.sliit.railconnect.auth.repository.UserRepository;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerPassenger(RegistrationForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessRuleException("Passwords do not match.");
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new BusinessRuleException("An account already exists for this email address.");
        }
        User user = new User(
                form.getFullName().trim(),
                form.getEmail().trim(),
                passwordEncoder.encode(form.getPassword()),
                form.getPhone().trim(),
                UserRole.PASSENGER
        );
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User byEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User account was not found."));
    }

    @Transactional
    public User updateProfile(String email, ProfileForm form) {
        User user = byEmail(email);
        user.updateProfile(form.getFullName().trim(), form.getPhone().trim());
        return user;
    }

    @Transactional
    public void changePassword(String email, ChangePasswordForm form) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessRuleException("New passwords do not match.");
        }
        User user = byEmail(email);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect.");
        }
        user.changePassword(passwordEncoder.encode(form.getNewPassword()));
    }

    @Transactional
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30)));
        return token;
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordForm form) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessRuleException("Passwords do not match.");
        }
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("This password reset link is invalid."));
        if (!resetToken.isUsable(LocalDateTime.now())) {
            throw new BusinessRuleException("This password reset link has expired or has already been used.");
        }
        resetToken.getUser().changePassword(passwordEncoder.encode(form.getNewPassword()));
        resetToken.markUsed();
    }
}
