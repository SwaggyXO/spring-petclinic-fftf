package org.springframework.samples.petclinic.featureflag.exception;

public class FeatureFlagNotFoundException extends RuntimeException {
	public FeatureFlagNotFoundException(String message) {
		super(message);
	}
}
