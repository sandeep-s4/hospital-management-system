package com.hospital.management.controller;

import com.hospital.management.entity.Patient;
import com.hospital.management.service.CaptchaService;
import com.hospital.management.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final CaptchaService captchaService;
    private final PatientService patientService;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public AuthController(CaptchaService captchaService, PatientService patientService) {
        this.captchaService = captchaService;
        this.patientService = patientService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(Model model, HttpSession session) {
        model.addAttribute("captcha", captchaService.generate(session));
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String captcha,
                             HttpSession session,
                             Model model) {
        boolean validCaptcha = captchaService.validate(session, captcha);

        if (validCaptcha && ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("role", "ADMIN");
            session.setAttribute("loggedInUser", ADMIN_USERNAME);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error",
                !validCaptcha ? "Invalid CAPTCHA." : "Invalid admin username or password.");
        model.addAttribute("captcha", captchaService.generate(session));
        return "admin-login";
    }

    @GetMapping("/patient/login")
    public String patientLoginPage(Model model, HttpSession session) {
        model.addAttribute("captcha", captchaService.generate(session));
        return "patient-login";
    }

    @PostMapping("/patient/login")
    public String patientLogin(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String captcha,
                               HttpSession session,
                               Model model) {
        boolean validCaptcha = captchaService.validate(session, captcha);
        Patient patient = patientService.findByUsername(username);

        if (validCaptcha && patientService.passwordMatches(password, patient)) {
            session.setAttribute("role", "PATIENT");
            session.setAttribute("patientId", patient.getId());
            session.setAttribute("loggedInUser", patient.getUsername());
            return "redirect:/patient/dashboard";
        }

        model.addAttribute("error",
                !validCaptcha ? "Invalid CAPTCHA." : "Invalid patient username or password.");
        model.addAttribute("captcha", captchaService.generate(session));
        return "patient-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout";
    }
}
