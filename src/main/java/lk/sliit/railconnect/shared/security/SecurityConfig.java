package lk.sliit.railconnect.shared.security;

import lk.sliit.railconnect.auth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmailIgnoreCase(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .disabled(!user.isActive())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/search", "/forgot-password", "/reset-password", "/favicon.svg", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/admin/trains/**", "/admin/routes/**", "/admin/schedules/**", "/admin/carriages/**")
                        .hasRole("RAILWAY_ADMIN")
                        .requestMatchers("/admin/complaints/**", "/admin/bookings/**", "/admin/**")
                        .hasAnyRole("BOOKING_OFFICER", "RAILWAY_ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/post-login", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll())
                .exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"));
        return http.build();
    }
}
