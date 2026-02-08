package org.springframework.samples.petclinic.featureflag.dto;

import lombok.Data;
import org.springframework.samples.petclinic.featureflag.model.StrategyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

@Data
public class FeatureFlagDTO {

	@NotBlank(message = "Flag key is required")
	@Pattern(regexp = "^[a-z0-9_]+$", message = "Flag key must contain only lowercase letters, numbers, and underscores")
	private String flagKey;

	private String description;

	private Boolean enabled;

	private StrategyType strategyType;

	private Map<String, Object> strategyConfig;

	private String environment;

	private String createdBy;

	private String updatedBy;

	private String reason;
}
