package lk.sliit.railconnect.shared.web;

import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.auth.service.UserService;
import lk.sliit.railconnect.features.booking.domain.BookingStatus;
import lk.sliit.railconnect.features.booking.repository.TicketBookingRepository;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.repository.ComplaintRepository;
import lk.sliit.railconnect.features.route.service.RouteService;
import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.repository.TrainScheduleRepository;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.repository.TrainRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {
    private final UserService userService;
    private final TrainRepository trainRepository;
    private final TrainScheduleRepository scheduleRepository;
    private final TicketBookingRepository bookingRepository;
    private final ComplaintRepository complaintRepository;
    private final RouteService routeService;

    public HomeController(UserService userService, TrainRepository trainRepository,
                          TrainScheduleRepository scheduleRepository, TicketBookingRepository bookingRepository,
                          ComplaintRepository complaintRepository, RouteService routeService) {
        this.userService = userService;
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.complaintRepository = complaintRepository;
        this.routeService = routeService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("defaultDate", LocalDate.now());
        model.addAttribute("stations", routeService.activeStationNames());
        model.addAttribute("canSearch", authentication == null || authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ANONYMOUS")
                        || authority.getAuthority().equals("ROLE_PASSENGER")
                        || authority.getAuthority().equals("ROLE_BOOKING_OFFICER")));
        return "home";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        UserRole role = userService.byEmail(authentication.getName()).getRole();
        return role == UserRole.PASSENGER ? "redirect:/" : "redirect:/admin";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("activeTrains", trainRepository.countByStatus(TrainStatus.ACTIVE));
        model.addAttribute("todaySchedules", scheduleRepository.countByStatusAndTravelDate(ScheduleStatus.ACTIVE, LocalDate.now()));
        model.addAttribute("confirmedBookings", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("pendingComplaints", complaintRepository.countByStatusIn(List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_REVIEW)));
        model.addAttribute("recentBookings", bookingRepository.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        model.addAttribute("upcomingSchedules", scheduleRepository.findAllByOrderByTravelDateAscDepartureTimeAsc().stream()
                .filter(schedule -> !schedule.getTravelDate().isBefore(LocalDate.now())).limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", "You do not have permission to open that page.");
        return "errors/error";
    }
}
