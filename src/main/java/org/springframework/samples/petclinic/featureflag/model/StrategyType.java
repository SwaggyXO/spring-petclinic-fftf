package org.springframework.samples.petclinic.featureflag.model;

public enum StrategyType {
	BOOLEAN,
	PERCENTAGE,
	WHITELIST,
	BLACKLIST,
	USER_ATTRIBUTE,
	KILL_SWITCH
}
