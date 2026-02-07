package org.springframework.samples.petclinic.featureflag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.petclinic.featureflag.model.FeatureFlag;
import org.springframework.samples.petclinic.featureflag.model.FlagContext;
import org.springframework.samples.petclinic.featureflag.repository.FeatureFlagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FeatureFlagEvaluator {

	@Autowired
	private FeatureFlagRepository repository;

	@Cacheable(value = "featureFlags", key = "#flagKey")
	public boolean isEnabled(String flagKey, FlagContext context) {
		Optional<FeatureFlag> flagOpt = repository.findByFlagKeyAndEnvironment(
			flagKey, context.getEnvironment()
		);

		if (flagOpt.isEmpty()) {
			log.warn("Flag {} not found, defaulting to false", flagKey);
			return false;
		}

		FeatureFlag flag = flagOpt.get();

		if (!flag.getEnabled()) {
			return false;
		}

		return evaluateStrategy(flag, context);
	}

	private boolean evaluateStrategy(FeatureFlag flag, FlagContext context) {
		switch (flag.getStrategyType()) {
			case BOOLEAN:
				return true;

			case PERCENTAGE:
				return evaluatePercentage(flag, context);

			case WHITELIST:
				return evaluateWhitelist(flag, context);

			case BLACKLIST:
				return !evaluateBlacklist(flag, context);

			case USER_ATTRIBUTE:
				return evaluateUserAttribute(flag, context);

			case KILL_SWITCH:
				return false; // Always returns false for emergency shutoff

			default:
				return flag.getEnabled();
		}
	}

	private boolean evaluatePercentage(FeatureFlag flag, FlagContext context) {
		Integer percentage = (Integer) flag.getStrategyConfig().get("percentage");
		if (percentage == null || percentage <= 0) return false;
		if (percentage >= 100) return true;

		// Consistent hashing for same user
		String identifier = context.getUserId() != null ?
			context.getUserId() : context.getSessionId();

		if (identifier == null) return false;

		int hash = Math.abs((flag.getFlagKey() + identifier).hashCode());
		return (hash % 100) < percentage;
	}

	@SuppressWarnings("unchecked")
	private boolean evaluateWhitelist(FeatureFlag flag, FlagContext context) {
		List<String> whitelist = (List<String>) flag.getStrategyConfig().get("whitelist");
		if (whitelist == null || whitelist.isEmpty()) return false;

		return whitelist.contains(context.getUserId()) ||
			whitelist.contains(context.getIpAddress());
	}

	@SuppressWarnings("unchecked")
	private boolean evaluateBlacklist(FeatureFlag flag, FlagContext context) {
		List<String> blacklist = (List<String>) flag.getStrategyConfig().get("blacklist");
		if (blacklist == null || blacklist.isEmpty()) return false;

		return blacklist.contains(context.getUserId()) ||
			blacklist.contains(context.getIpAddress());
	}

	@SuppressWarnings("unchecked")
	private boolean evaluateUserAttribute(FeatureFlag flag, FlagContext context) {
		Map<String, String> requiredAttrs = (Map<String, String>)
			flag.getStrategyConfig().get("attributes");

		if (requiredAttrs == null || context.getAttributes() == null) return false;

		return requiredAttrs.entrySet().stream()
			.allMatch(entry -> entry.getValue().equals(
				context.getAttributes().get(entry.getKey())
			));
	}
}

