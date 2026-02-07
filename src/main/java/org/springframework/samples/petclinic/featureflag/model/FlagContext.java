package org.springframework.samples.petclinic.featureflag.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FlagContext {
	private String userId;
	private String sessionId;
	private String ipAddress;
	private String userRole;
	private Map<String, String> attributes;
	private String environment;
}
