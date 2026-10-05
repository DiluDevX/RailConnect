package lk.sliit.railconnect.features.train.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.features.train.domain.TrainStatus;
import lk.sliit.railconnect.features.train.dto.TrainForm;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/trains")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) { this.trainService = trainService; }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("trains", trainService.list(q));
        model.addAttribute("query", q);
        return "features/train/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("trainForm", new TrainForm());
        model.addAttribute("statuses", TrainStatus.values());
        model.addAttribute("editing", false);
        return "features/train/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute TrainForm trainForm, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return form(model, false);
        try { trainService.create(trainForm); }
        catch (BusinessRuleException exception) { result.reject("train", exception.getMessage()); return form(model, false); }
        redirectAttributes.addFlashAttribute("success", "Train created successfully.");
        return "redirect:/admin/trains";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("trainForm", TrainForm.from(trainService.require(id)));
        model.addAttribute("trainId", id);
        model.addAttribute("statuses", TrainStatus.values());
        model.addAttribute("editing", true);
        return "features/train/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute TrainForm trainForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) { model.addAttribute("trainId", id); return form(model, true); }
        try { trainService.update(id, trainForm); }
        catch (BusinessRuleException exception) { result.reject("train", exception.getMessage()); model.addAttribute("trainId", id); return form(model, true); }
        redirectAttributes.addFlashAttribute("success", "Train updated successfully.");
        return "redirect:/admin/trains";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        trainService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Train status updated.");
        return "redirect:/admin/trains";
    }

    private String form(Model model, boolean editing) {
        model.addAttribute("statuses", TrainStatus.values());
        model.addAttribute("editing", editing);
        return "features/train/form";
    }
}
