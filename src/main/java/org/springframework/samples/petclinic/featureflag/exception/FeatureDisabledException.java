package org.springframework.samples.petclinic.featureflag.exception;

public class FeatureDisabledException extends RuntimeException {
	public FeatureDisabledException(String message) {
		super(message);
	}
}
