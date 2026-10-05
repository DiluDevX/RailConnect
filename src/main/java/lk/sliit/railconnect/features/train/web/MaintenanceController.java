package lk.sliit.railconnect.features.train.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.features.train.domain.MaintenanceStatus;
import lk.sliit.railconnect.features.train.dto.MaintenanceForm;
import lk.sliit.railconnect.features.train.service.MaintenanceService;
import lk.sliit.railconnect.features.train.service.TrainService;
import lk.sliit.railconnect.shared.exception.BusinessRuleException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/admin/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;
    private final TrainService trainService;

    public MaintenanceController(MaintenanceService maintenanceService, TrainService trainService) {
        this.maintenanceService = maintenanceService;
        this.trainService = trainService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long trainId,
                       @RequestParam(required = false) MaintenanceStatus status,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {
        model.addAttribute("records", maintenanceService.search(trainId, status, type, date));
        model.addAttribute("trains", trainService.list(null));
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("selectedTrainId", trainId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("type", type);
        model.addAttribute("date", date);
        return "features/maintenance/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long trainId, Model model) {
        MaintenanceForm form = new MaintenanceForm();
        form.setTrainId(trainId);
        form.setMaintenanceDate(LocalDate.now());
        form.setStatus(MaintenanceStatus.SCHEDULED);
        return form(model, form, false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("maintenanceForm") MaintenanceForm form, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return form(model, form, false);
        }
        maintenanceService.create(form);
        redirectAttributes.addFlashAttribute("success", "Maintenance created successfully.");
        return "redirect:/admin/maintenance";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("maintenanceId", id);
        return form(model, MaintenanceForm.from(maintenanceService.require(id)), true);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("maintenanceForm") MaintenanceForm form,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("maintenanceId", id);
            return form(model, form, true);
        }
        maintenanceService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Maintenance updated successfully.");
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Maintenance cancelled.");
        } catch (BusinessRuleException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceService.deleteScheduled(id);
            redirectAttributes.addFlashAttribute("success", "Scheduled maintenance deleted.");
        } catch (BusinessRuleException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/maintenance";
    }

    private String form(Model model, MaintenanceForm form, boolean editing) {
        model.addAttribute("maintenanceForm", form);
        model.addAttribute("trains", trainService.list(null));
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("editing", editing);
        return "features/maintenance/form";
    }
}
