package com.hospital.management.exception;

public class PatientNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
    public PatientNotFoundException(Long id) {
        super("Patient with ID " + id + " was not found.");
    }
}
