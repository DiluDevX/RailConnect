package lk.sliit.railconnect.shared.security;

import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserService userService;

    public CurrentUserService(UserService userService) {
        this.userService = userService;
    }

    public User require(Authentication authentication) {
        return userService.byEmail(authentication.getName());
    }
}
