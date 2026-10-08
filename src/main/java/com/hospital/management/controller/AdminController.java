package com.hospital.management.controller;

import com.hospital.management.entity.Patient;
import com.hospital.management.exception.DuplicateUsernameException;
import com.hospital.management.exception.PatientValidationException;
import com.hospital.management.service.CaptchaService;
import com.hospital.management.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final PatientService patientService;
    private final CaptchaService captchaService;

    public AdminController(PatientService patientService, CaptchaService captchaService) {
        this.patientService = patientService;
        this.captchaService = captchaService;
    }

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("patient", new Patient());
        addDashboardData(model, session);
        return "admin-dashboard";
    }

    @PostMapping("/patients/save")
    public String savePatient(@ModelAttribute Patient patient,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";

        try {
            patientService.save(patient);
            return "redirect:/admin/dashboard?saved";
        } catch (PatientValidationException ex) {
            model.addAttribute("fieldErrors", ex.getErrors());
            addDashboardData(model, session);
            return "admin-dashboard";
        } catch (DuplicateUsernameException ex) {
            model.addAttribute("fieldErrors", java.util.Map.of("username", ex.getMessage()));
            addDashboardData(model, session);
            return "admin-dashboard";
        }
    }

    @GetMapping("/patients/edit/{id}")
    public String editPatient(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        Patient patient = patientService.findById(id);
        // Never put the stored BCrypt hash into the password input.
        patient.setPassword("");
        model.addAttribute("patient", patient);
        addDashboardData(model, session);
        return "admin-dashboard";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        patientService.delete(id);
        return "redirect:/admin/dashboard?deleted";
    }

    private void addDashboardData(Model model, HttpSession session) {
        model.addAttribute("patients", patientService.findAll());
        model.addAttribute("captcha", captchaService.generate(session));
    }
}
