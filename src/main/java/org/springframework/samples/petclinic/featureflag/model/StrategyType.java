package org.springframework.samples.petclinic.featureflag.model;

public enum StrategyType {
	BOOLEAN,
	PERCENTAGE,
	WHITELIST,
	BLACKLIST,
	USER_ATTRIBUTE,
	DATETIME_RANGE,
	KILL_SWITCH
}
