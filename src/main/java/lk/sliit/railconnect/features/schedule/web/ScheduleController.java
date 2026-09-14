package lk.sliit.railconnect.features.schedule.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.features.route.service.RouteService;
import lk.sliit.railconnect.features.schedule.domain.ScheduleStatus;
import lk.sliit.railconnect.features.schedule.dto.ScheduleForm;
import lk.sliit.railconnect.features.schedule.service.ScheduleService;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final TrainService trainService;
    private final RouteService routeService;

    public ScheduleController(ScheduleService scheduleService, TrainService trainService, RouteService routeService) {
        this.scheduleService = scheduleService;
        this.trainService = trainService;
        this.routeService = routeService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String from,
                         @RequestParam(required = false) String to,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam(defaultValue = "1") int passengers,
                         Authentication authentication, Model model) {
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(authority ->
                authority.getAuthority().equals("ROLE_ANONYMOUS")
                        || authority.getAuthority().equals("ROLE_PASSENGER")
                        || authority.getAuthority().equals("ROLE_BOOKING_OFFICER"))) {
            throw new AccessDeniedException("Staff administrators cannot use passenger booking search.");
        }
        int requestedPassengers = Math.max(1, Math.min(passengers, 10));
        LocalDate requestedDate = date == null ? LocalDate.now() : date;
        model.addAttribute("schedules", scheduleService.search(from, to, requestedDate, requestedPassengers));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("date", requestedDate);
        model.addAttribute("passengers", requestedPassengers);
        model.addAttribute("stations", routeService.activeStationNames());
        return "features/schedule/search-results";
    }

    @GetMapping("/admin/schedules")
    public String list(Model model) {
        model.addAttribute("schedules", scheduleService.list());
        return "features/schedule/list";
    }

    @GetMapping("/admin/schedules/new")
    public String createForm(Model model) {
        ScheduleForm form = new ScheduleForm();
        form.setTravelDate(LocalDate.now());
        model.addAttribute("scheduleForm", form);
        return form(model, false);
    }

    @PostMapping("/admin/schedules")
    public String create(@Valid @ModelAttribute ScheduleForm scheduleForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return form(model, false);
        try { scheduleService.create(scheduleForm); }
        catch (BusinessRuleException exception) { result.reject("schedule", exception.getMessage()); return form(model, false); }
        redirectAttributes.addFlashAttribute("success", "Schedule created successfully.");
        return "redirect:/admin/schedules";
    }

    @GetMapping("/admin/schedules/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("scheduleForm", ScheduleForm.from(scheduleService.require(id)));
        model.addAttribute("scheduleId", id);
        return form(model, true);
    }

    @PostMapping("/admin/schedules/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ScheduleForm scheduleForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) { model.addAttribute("scheduleId", id); return form(model, true); }
        try { scheduleService.update(id, scheduleForm); }
        catch (BusinessRuleException exception) { result.reject("schedule", exception.getMessage()); model.addAttribute("scheduleId", id); return form(model, true); }
        redirectAttributes.addFlashAttribute("success", "Schedule updated successfully.");
        return "redirect:/admin/schedules";
    }

    @PostMapping("/admin/schedules/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        scheduleService.cancel(id);
        redirectAttributes.addFlashAttribute("success", "Schedule cancelled. Historical records were preserved.");
        return "redirect:/admin/schedules";
    }

    private String form(Model model, boolean editing) {
        model.addAttribute("trains", trainService.active());
        model.addAttribute("routes", routeService.active());
        model.addAttribute("statuses", ScheduleStatus.values());
        model.addAttribute("editing", editing);
        return "features/schedule/form";
    }
}
