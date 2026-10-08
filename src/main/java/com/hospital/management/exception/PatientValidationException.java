package com.hospital.management.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when patient data does not satisfy the application's validation rules.
 */
public class PatientValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
    private final Map<String, String> errors;

    public PatientValidationException(Map<String, String> errors) {
        super("Patient data is invalid");
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
