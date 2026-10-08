package com.hospital.management.controller;

import com.hospital.management.entity.Patient;
import com.hospital.management.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patient/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!"PATIENT".equals(session.getAttribute("role"))) return "redirect:/patient/login";
        Long id = (Long) session.getAttribute("patientId");
        Patient patient = patientService.findById(id);
        model.addAttribute("patient", patient);
        return "patient-dashboard";
    }
}
