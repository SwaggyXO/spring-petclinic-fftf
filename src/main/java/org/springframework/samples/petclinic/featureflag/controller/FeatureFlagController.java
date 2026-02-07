package org.springframework.samples.petclinic.featureflag.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.featureflag.dto.FeatureFlagDTO;
import org.springframework.samples.petclinic.featureflag.model.FeatureFlag;
import org.springframework.samples.petclinic.featureflag.model.FlagAudit;
import org.springframework.samples.petclinic.featureflag.model.FlagContext;
import org.springframework.samples.petclinic.featureflag.service.FeatureFlagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/flags")
@Slf4j
public class FeatureFlagController {

	@Autowired
	private FeatureFlagService service;

	@GetMapping
	public ResponseEntity<List<FeatureFlag>> getAllFlags(
		@RequestParam(defaultValue = "development") String environment
	) {
		return ResponseEntity.ok(service.getAllFlags(environment));
	}

	@GetMapping("/{flagKey}")
	public ResponseEntity<FeatureFlag> getFlag(@PathVariable String flagKey) {
		return service.getFlag(flagKey)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<FeatureFlag> createFlag(@RequestBody @Valid FeatureFlagDTO dto) {
		FeatureFlag created = service.createFlag(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{flagKey}")
	public ResponseEntity<FeatureFlag> updateFlag(
		@PathVariable String flagKey,
		@RequestBody @Valid FeatureFlagDTO dto
	) {
		return ResponseEntity.ok(service.updateFlag(flagKey, dto));
	}

	@DeleteMapping("/{flagKey}")
	public ResponseEntity<Void> deleteFlag(@PathVariable String flagKey) {
		service.deleteFlag(flagKey);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{flagKey}/toggle")
	public ResponseEntity<FeatureFlag> toggleFlag(@PathVariable String flagKey) {
		return ResponseEntity.ok(service.toggleFlag(flagKey));
	}

	@GetMapping("/{flagKey}/evaluate")
	public ResponseEntity<Map<String, Boolean>> evaluateFlag(
		@PathVariable String flagKey,
		@RequestParam(required = false) String userId,
		@RequestParam(required = false) String sessionId
	) {
		FlagContext context = FlagContext.builder()
			.userId(userId)
			.sessionId(sessionId)
			.environment(System.getProperty("spring.profiles.active", "development"))
			.build();

		boolean enabled = service.evaluate(flagKey, context);
		return ResponseEntity.ok(Map.of("enabled", enabled));
	}

	@GetMapping("/{flagKey}/audit")
	public ResponseEntity<List<FlagAudit>> getAuditLog(@PathVariable String flagKey) {
		return ResponseEntity.ok(service.getAuditLog(flagKey));
	}
}

