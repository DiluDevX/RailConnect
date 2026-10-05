package lk.sliit.railconnect.features.carriage.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.features.carriage.domain.CarriageClass;
import lk.sliit.railconnect.features.carriage.domain.CarriageStatus;
import lk.sliit.railconnect.features.carriage.dto.CarriageForm;
import lk.sliit.railconnect.features.carriage.service.CarriageService;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/carriages")
public class CarriageController {
    private final CarriageService carriageService;
    private final TrainService trainService;

    public CarriageController(CarriageService carriageService, TrainService trainService) {
        this.carriageService = carriageService;
        this.trainService = trainService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("carriages", carriageService.list());
        return "features/carriage/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("carriageForm", new CarriageForm());
        return form(model, false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CarriageForm carriageForm, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        // Field-level validation: checks @Valid annotations on CarriageForm (e.g. required fields, formats)
        if (result.hasErrors()) return form(model, false);

        // Business-rule validation: rules enforced inside the service layer
        // (e.g. seat layout must be valid for the chosen carriage class, train must exist)
        try { carriageService.create(carriageForm); }
        catch (BusinessRuleException exception) {
            // Attach the business-rule failure as a form-level error and redisplay the form
            result.reject("carriage", exception.getMessage());
            return form(model, false);
        }
        redirectAttributes.addFlashAttribute("success", "Carriage and seats created successfully.");
        return "redirect:/admin/carriages";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("carriageForm", CarriageForm.from(carriageService.require(id)));
        model.addAttribute("carriageId", id);
        return form(model, true);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute CarriageForm carriageForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // Field-level validation, same as create()
        if (result.hasErrors()) { model.addAttribute("carriageId", id); return form(model, true); }

        // Business-rule validation, same pattern as create() but scoped to the existing carriage id
        // (e.g. can't shrink seat count below already-booked seats)
        try { carriageService.update(id, carriageForm); }
        catch (BusinessRuleException exception) {
            result.reject("carriage", exception.getMessage());
            model.addAttribute("carriageId", id);
            return form(model, true);
        }
        redirectAttributes.addFlashAttribute("success", "Carriage updated successfully.");
        return "redirect:/admin/carriages";
    }

    @GetMapping("/{id}/seats")
    public String seats(@PathVariable Long id, Model model) {
        model.addAttribute("carriage", carriageService.require(id));
        model.addAttribute("seats", carriageService.seats(id));
        return "features/carriage/seats";
    }

    @PostMapping("/{carriageId}/seats/{seatId}/toggle")
    public String toggleSeat(@PathVariable Long carriageId, @PathVariable Long seatId,
                             RedirectAttributes redirectAttributes) {
        // No form input to validate here — but toggleSeat is still expected to enforce
        // internal state rules (e.g. can't disable a seat that's part of an active booking)
        carriageService.toggleSeat(seatId);
        redirectAttributes.addFlashAttribute("success", "Seat status updated.");
        return "redirect:/admin/carriages/" + carriageId + "/seats";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // No form input to validate here — toggling status has no user input to check
        carriageService.toggleCarriage(id);
        redirectAttributes.addFlashAttribute("success", "Carriage status updated.");
        return "redirect:/admin/carriages";
    }

    private String form(Model model, boolean editing) {
        model.addAttribute("trains", trainService.list(null));
        model.addAttribute("classes", CarriageClass.values());
        model.addAttribute("statuses", CarriageStatus.values());
        model.addAttribute("editing", editing);
        return "features/carriage/form";
    }
}