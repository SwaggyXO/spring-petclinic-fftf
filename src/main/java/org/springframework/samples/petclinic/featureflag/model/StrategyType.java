package org.springframework.samples.petclinic.featureflag.model;

public enum StrategyType {
	BOOLEAN,           // Simple on/off
	PERCENTAGE,        // Rollout to X% of users
	WHITELIST,         // Allow specific users/IPs
	BLACKLIST,         // Block specific users/IPs
	USER_ATTRIBUTE,    // Based on user properties
	DATETIME_RANGE,    // Enable between dates
	KILL_SWITCH        // Emergency disable
}
