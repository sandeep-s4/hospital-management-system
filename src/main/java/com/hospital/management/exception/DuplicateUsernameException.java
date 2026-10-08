package com.hospital.management.exception;

public class DuplicateUsernameException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
    public DuplicateUsernameException(String username) {
        super("Username '" + username + "' is already in use.");
    }
}
