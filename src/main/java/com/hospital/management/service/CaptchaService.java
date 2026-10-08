package com.hospital.management.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CaptchaService {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String KEY = "CAPTCHA_CODE";
    private final SecureRandom random = new SecureRandom();

    public String generate(HttpSession session) {
        StringBuilder value = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            value.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        String captcha = value.toString();
        session.setAttribute(KEY, captcha);
        return captcha;
    }

    public boolean validate(HttpSession session, String input) {
        Object expected = session.getAttribute(KEY);
        session.removeAttribute(KEY);
        return expected != null && input != null && expected.toString().equalsIgnoreCase(input.trim());
    }
}
