package org.springframework.samples.petclinic.featureflag.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FeatureFlagNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(FeatureFlagNotFoundException ex) {
		return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DuplicateFlagException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateFlagException ex) {
		return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(FeatureDisabledException.class)
	public ResponseEntity<Map<String, Object>> handleFeatureDisabled(FeatureDisabledException ex) {
		return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

	private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
		Map<String, Object> error = new HashMap<>();
		error.put("timestamp", LocalDateTime.now());
		error.put("status", status.value());
		error.put("error", status.getReasonPhrase());
		error.put("message", message);
		return ResponseEntity.status(status).body(error);
	}
}
