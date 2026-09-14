package lk.sliit.railconnect.features.booking.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.auth.domain.UserRole;
import lk.sliit.railconnect.features.booking.domain.PaymentMethod;
import lk.sliit.railconnect.features.booking.domain.TicketBooking;
import lk.sliit.railconnect.features.booking.dto.BookingForm;
import lk.sliit.railconnect.features.booking.service.BookingService;
import lk.sliit.railconnect.features.booking.service.BookingPdfService;
import lk.sliit.railconnect.features.booking.service.BookingPaymentService;
import lk.sliit.railconnect.features.schedule.domain.TrainSchedule;
import lk.sliit.railconnect.features.schedule.service.ScheduleService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@Controller
public class BookingController {
    private final BookingService bookingService;
    private final ScheduleService scheduleService;
    private final CurrentUserService currentUserService;
    private final BookingPdfService bookingPdfService;
    private final BookingPaymentService bookingPaymentService;

    public BookingController(BookingService bookingService, ScheduleService scheduleService,
                             CurrentUserService currentUserService, BookingPdfService bookingPdfService,
                             BookingPaymentService bookingPaymentService) {
        this.bookingService = bookingService;
        this.scheduleService = scheduleService;
        this.currentUserService = currentUserService;
        this.bookingPdfService = bookingPdfService;
        this.bookingPaymentService = bookingPaymentService;
    }

    @GetMapping("/schedules/{scheduleId}/book")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String seatSelection(@PathVariable Long scheduleId, Authentication authentication, Model model) {
        User user = currentUserService.require(authentication);
        if (!model.containsAttribute("bookingForm")) {
            BookingForm form = new BookingForm();
            form.setPassengerName(user.getRole() == UserRole.BOOKING_OFFICER ? "Customer" : user.getFullName());
            if (user.getRole() != UserRole.BOOKING_OFFICER) {
                form.setContactEmail(user.getEmail());
                form.setContactPhone(user.getPhone());
            }
            model.addAttribute("bookingForm", form);
        }
        return seatPage(scheduleId, user, model);
    }

    @PostMapping("/schedules/{scheduleId}/book")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String startBooking(@PathVariable Long scheduleId, @Valid @ModelAttribute BookingForm bookingForm,
                               BindingResult result, Authentication authentication, Model model) {
        User actor = currentUserService.require(authentication);
        if (result.hasErrors()) return seatPage(scheduleId, actor, model);
        try {
            TicketBooking booking = bookingService.startBooking(actor, scheduleId, bookingForm);
            return "redirect:/bookings/" + booking.getId() + "/payment";
        } catch (BusinessRuleException exception) {
            result.reject("booking", exception.getMessage());
            return seatPage(scheduleId, actor, model);
        }
    }

    @GetMapping("/bookings/{id}/payment")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String payment(@PathVariable Long id, Authentication authentication, Model model) {
        User actor = currentUserService.require(authentication);
        TicketBooking booking = bookingService.requireAccessible(id, actor);
        if (booking.getStatus() != lk.sliit.railconnect.features.booking.domain.BookingStatus.PENDING_PAYMENT) {
            return "redirect:/bookings/" + id;
        }
        model.addAttribute("booking", booking);
        model.addAttribute("bookingSeats", bookingService.seatsForBooking(id));
        model.addAttribute("cashPayment", actor.getRole() == UserRole.BOOKING_OFFICER);
        return "features/booking/payment";
    }

    @PostMapping("/bookings/{id}/payment")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String pay(@PathVariable Long id, @RequestParam(defaultValue = "success") String outcome,
                      @RequestParam(required = false) String cardholderName,
                      @RequestParam(required = false) String cardNumber,
                      @RequestParam(required = false) String expiry,
                      @RequestParam(required = false) String cvv,
                      @RequestParam(required = false) BigDecimal cashReceived,
                      Authentication authentication, RedirectAttributes redirectAttributes) {
        User actor = currentUserService.require(authentication);
        TicketBooking pendingBooking = bookingService.requireAccessible(id, actor);
        PaymentMethod method;
        boolean successful;
        if (actor.getRole() == UserRole.BOOKING_OFFICER) {
            method = PaymentMethod.CASH;
            if (cashReceived == null || cashReceived.compareTo(pendingBooking.getTotalAmount()) < 0) {
                redirectAttributes.addFlashAttribute("error", "Cash received must cover the full booking total.");
                return "redirect:/bookings/" + id + "/payment";
            }
            successful = true;
        } else {
            method = PaymentMethod.SIMULATED_CARD;
            String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
            boolean validCard = cardholderName != null && !cardholderName.isBlank()
                    && digits.matches("4\\d{15}")
                    && expiry != null && expiry.matches("(0[1-9]|1[0-2])/\\d{2}")
                    && cvv != null && cvv.matches("\\d{3}");
            if (!validCard) {
                redirectAttributes.addFlashAttribute("error", "Enter the demo Visa details in the requested format.");
                return "redirect:/bookings/" + id + "/payment";
            }
            successful = "success".equalsIgnoreCase(outcome);
        }
        TicketBooking booking = bookingPaymentService.processPayment(id, actor, method, successful);
        if (successful) {
            String message = method == PaymentMethod.CASH
                    ? "Cash payment recorded. The assisted booking is confirmed."
                    : "Demo Visa payment approved. Your booking is confirmed.";
            redirectAttributes.addFlashAttribute("success", message);
        } else {
            redirectAttributes.addFlashAttribute("error", "Simulated payment failed. Your held seats were released.");
        }
        return "redirect:/bookings/" + booking.getId();
    }

    @PostMapping("/bookings/{id}/retry")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String retry(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        TicketBooking booking = bookingService.retry(id, currentUserService.require(authentication));
        redirectAttributes.addFlashAttribute("success", "Seats were reserved again. Complete the simulated payment.");
        return "redirect:/bookings/" + booking.getId() + "/payment";
    }

    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER','BOOKING_OFFICER')")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        bookingService.cancel(id, currentUserService.require(authentication));
        redirectAttributes.addFlashAttribute("success", "Booking cancelled and its seats released. Any payment refund was simulated.");
        return "redirect:/bookings/" + id;
    }

    @GetMapping("/bookings/{id}")
    public String details(@PathVariable Long id, Authentication authentication, Model model) {
        User actor = currentUserService.require(authentication);
        model.addAttribute("booking", bookingService.requireAccessible(id, actor));
        model.addAttribute("bookingOfficer", actor.getRole() == UserRole.BOOKING_OFFICER);
        model.addAttribute("bookingSeats", bookingService.seatsForBooking(id));
        model.addAttribute("payments", bookingService.paymentsForBooking(id));
        return "features/booking/details";
    }

    @GetMapping(value = "/bookings/{id}/ticket.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> ticketPdf(@PathVariable Long id, Authentication authentication) {
        TicketBooking booking = bookingService.requireAccessible(id, currentUserService.require(authentication));
        byte[] pdf = bookingPdfService.createTicket(booking);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"railconnect-" + booking.getBookingReference() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('PASSENGER')")
    public String myBookings(Authentication authentication, Model model) {
        model.addAttribute("bookings", bookingService.forUser(currentUserService.require(authentication)));
        return "features/booking/my-list";
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(Model model) {
        model.addAttribute("bookings", bookingService.all());
        return "features/booking/admin-list";
    }

    private String seatPage(Long scheduleId, User actor, Model model) {
        TrainSchedule schedule = scheduleService.require(scheduleId);
        model.addAttribute("schedule", schedule);
        model.addAttribute("seatGroups", bookingService.seatGroups(scheduleId));
        model.addAttribute("bookingActor", actor);
        model.addAttribute("bookingOfficer", actor.getRole() == UserRole.BOOKING_OFFICER);
        return "features/booking/seat-selection";
    }
}
