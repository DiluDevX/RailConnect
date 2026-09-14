package lk.sliit.railconnect.features.route.web;

import jakarta.validation.Valid;
import lk.sliit.railconnect.features.route.domain.RouteStatus;
import lk.sliit.railconnect.features.route.dto.RouteForm;
import lk.sliit.railconnect.features.route.service.RouteService;
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
@RequestMapping("/admin/routes")
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) { this.routeService = routeService; }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("routes", routeService.list(q));
        model.addAttribute("query", q);
        return "features/route/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("routeForm", new RouteForm());
        return form(model, false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute RouteForm routeForm, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return form(model, false);
        try { routeService.create(routeForm); }
        catch (BusinessRuleException exception) { result.reject("route", exception.getMessage()); return form(model, false); }
        redirectAttributes.addFlashAttribute("success", "Route created successfully.");
        return "redirect:/admin/routes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("routeForm", RouteForm.from(routeService.require(id)));
        model.addAttribute("routeId", id);
        return form(model, true);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute RouteForm routeForm,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) { model.addAttribute("routeId", id); return form(model, true); }
        try { routeService.update(id, routeForm); }
        catch (BusinessRuleException exception) { result.reject("route", exception.getMessage()); model.addAttribute("routeId", id); return form(model, true); }
        redirectAttributes.addFlashAttribute("success", "Route updated successfully.");
        return "redirect:/admin/routes";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        routeService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Route status updated.");
        return "redirect:/admin/routes";
    }

    private String form(Model model, boolean editing) {
        model.addAttribute("statuses", RouteStatus.values());
        model.addAttribute("editing", editing);
        return "features/route/form";
    }
}
