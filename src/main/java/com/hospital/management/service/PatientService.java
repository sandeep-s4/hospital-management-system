package com.hospital.management.service;

import com.hospital.management.entity.Patient;
import com.hospital.management.exception.DuplicateUsernameException;
import com.hospital.management.exception.PatientNotFoundException;
import com.hospital.management.exception.PatientValidationException;
import com.hospital.management.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {
    private final PatientRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Patient findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new PatientNotFoundException(id));
    }

    public Patient findByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    @Transactional
    public Patient save(Patient patient) {
        validate(patient);

        boolean newPatient = patient.getId() == null;
        Patient existing = null;
        if (!newPatient) {
            existing = findById(patient.getId());
        }

        if (usernameExists(patient.getUsername(), patient.getId())) {
            throw new DuplicateUsernameException(patient.getUsername());
        }

        if (newPatient) {
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        } else if (patient.getPassword() == null || patient.getPassword().isBlank()) {
            patient.setPassword(existing.getPassword());
        } else {
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
        }

        return repository.save(patient);
    }

    public boolean passwordMatches(String rawPassword, Patient patient) {
        return patient != null
                && rawPassword != null
                && patient.getPassword() != null
                && passwordEncoder.matches(rawPassword, patient.getPassword());
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    public boolean usernameExists(String username, Long currentId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        Patient existing = repository.findByUsername(username.trim()).orElse(null);
        return existing != null && (currentId == null || !existing.getId().equals(currentId));
    }

    private void validate(Patient patient) {
        Map<String, String> errors = new LinkedHashMap<>();

        String username = patient.getUsername() == null ? "" : patient.getUsername().trim();
        String password = patient.getPassword();
        String mobile = patient.getMobileNumber() == null ? "" : patient.getMobileNumber().trim();
        String address = patient.getAddress() == null ? "" : patient.getAddress().trim();
        BigDecimal paid = patient.getPaidAmount();
        BigDecimal total = patient.getTotalAmount();

        if (username.isEmpty()) {
            errors.put("username", "Username is required.");
        } else if (username.length() < 3 || username.length() > 50) {
            errors.put("username", "Username must be between 3 and 50 characters.");
        }

        if (patient.getId() == null && (password == null || password.isBlank())) {
            errors.put("password", "Password is required for a new patient.");
        } else if (password != null && !password.isBlank() && (password.length() < 4 || password.length() > 100)) {
            errors.put("password", "Password must be between 4 and 100 characters.");
        }

        if (patient.getDob() == null) {
            errors.put("dob", "Date of birth is required.");
        } else if (patient.getDob().isAfter(java.time.LocalDate.now())) {
            errors.put("dob", "Date of birth cannot be in the future.");
        }

        if (mobile.isEmpty()) {
            errors.put("mobileNumber", "Mobile number is required.");
        } else if (!mobile.matches("\\d{10}")) {
            errors.put("mobileNumber", "Mobile number must contain exactly 10 digits.");
        }

        if (address.isEmpty()) {
            errors.put("address", "Address is required.");
        } else if (address.length() > 500) {
            errors.put("address", "Address cannot exceed 500 characters.");
        }

        if (paid == null) {
            errors.put("paidAmount", "Paid amount is required.");
        } else if (paid.compareTo(BigDecimal.ZERO) < 0) {
            errors.put("paidAmount", "Paid amount cannot be negative.");
        }

        if (total == null) {
            errors.put("totalAmount", "Total amount is required.");
        } else if (total.compareTo(BigDecimal.ZERO) < 0) {
            errors.put("totalAmount", "Total amount cannot be negative.");
        }

        if (paid != null && total != null && paid.compareTo(total) > 0) {
            errors.put("paidAmount", "Paid amount cannot exceed total amount.");
        }

        if (!errors.isEmpty()) {
            throw new PatientValidationException(errors);
        }

        patient.setUsername(username);
        patient.setMobileNumber(mobile);
        patient.setAddress(address);
    }
}
