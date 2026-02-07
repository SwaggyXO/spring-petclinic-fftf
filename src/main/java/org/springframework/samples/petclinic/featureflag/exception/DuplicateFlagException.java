package org.springframework.samples.petclinic.featureflag.exception;

public class DuplicateFlagException extends RuntimeException {
	public DuplicateFlagException(String message) {
		super(message);
	}
}
