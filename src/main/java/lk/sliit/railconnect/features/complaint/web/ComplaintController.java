package lk.sliit.railconnect.features.complaint.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.auth.domain.User;
import lk.sliit.railconnect.features.booking.service.BookingService;
import lk.sliit.railconnect.features.complaint.domain.ComplaintStatus;
import lk.sliit.railconnect.features.complaint.domain.ComplaintType;
import lk.sliit.railconnect.features.complaint.dto.ComplaintForm;
import lk.sliit.railconnect.features.complaint.service.ComplaintService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import lk.sliit.railconnect.shared.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ComplaintController {
    private final ComplaintService complaintService;
    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    public ComplaintController(ComplaintService complaintService, BookingService bookingService,
                               CurrentUserService currentUserService) {
        this.complaintService = complaintService;
        this.bookingService = bookingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/my-complaints")
    public String myComplaints(Authentication authentication, Model model) {
        model.addAttribute("complaints", complaintService.forUser(currentUserService.require(authentication)));
        return "features/complaint/my-list";
    }

    @GetMapping("/complaints/new")
    public String createForm(Authentication authentication, Model model) {
        User user = currentUserService.require(authentication);
        model.addAttribute("complaintForm", new ComplaintForm());
        return form(model, user, false);
    }

    @PostMapping("/complaints")
    public String create(@Valid @ModelAttribute ComplaintForm complaintForm, BindingResult result,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        User user = currentUserService.require(authentication);
        if (result.hasErrors()) return form(model, user, false);
        try { complaintService.create(user, complaintForm); }
        catch (BusinessRuleException exception) { result.reject("complaint", exception.getMessage()); return form(model, user, false); }
        redirectAttributes.addFlashAttribute("success", "Complaint submitted successfully.");
        return "redirect:/my-complaints";
    }

    @GetMapping("/complaints/{id}")
    public String details(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("complaint", complaintService.requireAccessible(id, currentUserService.require(authentication)));
        return "features/complaint/details";
    }

    @GetMapping("/complaints/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        User user = currentUserService.require(authentication);
        model.addAttribute("complaintForm", ComplaintForm.from(complaintService.requireAccessible(id, user)));
        model.addAttribute("complaintId", id);
        return form(model, user, true);
    }

    @PostMapping("/complaints/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ComplaintForm complaintForm,
                         BindingResult result, Authentication authentication, Model model,
                         RedirectAttributes redirectAttributes) {
        User user = currentUserService.require(authentication);
        if (result.hasErrors()) { model.addAttribute("complaintId", id); return form(model, user, true); }
        try { complaintService.updateByPassenger(id, user, complaintForm); }
        catch (BusinessRuleException exception) { result.reject("complaint", exception.getMessage()); model.addAttribute("complaintId", id); return form(model, user, true); }
        redirectAttributes.addFlashAttribute("success", "Complaint updated.");
        return "redirect:/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/withdraw")
    public String withdraw(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        complaintService.withdraw(id, currentUserService.require(authentication));
        redirectAttributes.addFlashAttribute("success", "Complaint withdrawn.");
        return "redirect:/my-complaints";
    }

    @GetMapping("/admin/complaints")
    public String adminList(Model model) {
        model.addAttribute("complaints", complaintService.all());
        return "features/complaint/admin-list";
    }

    @GetMapping("/admin/complaints/{id}")
    public String adminDetails(@PathVariable Long id, Model model) {
        model.addAttribute("complaint", complaintService.require(id));
        model.addAttribute("statuses", ComplaintStatus.values());
        return "features/complaint/admin-details";
    }

    @PostMapping("/admin/complaints/{id}/respond")
    public String respond(@PathVariable Long id, @RequestParam ComplaintStatus status,
                          @RequestParam String response, RedirectAttributes redirectAttributes) {
        complaintService.respond(id, status, response);
        redirectAttributes.addFlashAttribute("success", "Complaint response saved.");
        return "redirect:/admin/complaints/" + id;
    }

    private String form(Model model, User user, boolean editing) {
        model.addAttribute("types", ComplaintType.values());
        model.addAttribute("bookings", bookingService.forUser(user));
        model.addAttribute("editing", editing);
        return "features/complaint/form";
    }
}
