package org.springframework.samples.petclinic.featureflag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.samples.petclinic.featureflag.model.FeatureFlag;
import org.springframework.samples.petclinic.featureflag.model.FlagAudit;
import org.springframework.samples.petclinic.featureflag.model.FlagContext;
import org.springframework.samples.petclinic.featureflag.repository.FeatureFlagRepository;
import org.springframework.samples.petclinic.featureflag.repository.FlagAuditRepository;
import org.springframework.samples.petclinic.featureflag.dto.FeatureFlagDTO;
import org.springframework.samples.petclinic.featureflag.exception.FeatureFlagNotFoundException;
import org.springframework.samples.petclinic.featureflag.exception.DuplicateFlagException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class FeatureFlagService {

	@Autowired
	private FeatureFlagRepository flagRepository;

	@Autowired
	private FlagAuditRepository auditRepository;

	@Autowired
	private FeatureFlagEvaluator evaluator;

	public List<FeatureFlag> getAllFlags(String environment) {
		log.info("Fetching all flags for environment: {}", environment);
		return flagRepository.findByEnvironment(environment);
	}

	@Cacheable(value = "featureFlags", key = "#flagKey")
	public Optional<FeatureFlag> getFlag(String flagKey) {
		return flagRepository.findByFlagKey(flagKey);
	}

	@CacheEvict(value = "featureFlags", key = "#dto.flagKey")
	public FeatureFlag createFlag(FeatureFlagDTO dto) {
		log.info("Creating new flag: {}", dto.getFlagKey());

		if (flagRepository.existsByFlagKey(dto.getFlagKey())) {
			throw new DuplicateFlagException("Flag with key '" + dto.getFlagKey() + "' already exists");
		}

		FeatureFlag flag = new FeatureFlag();
		flag.setFlagKey(dto.getFlagKey());
		flag.setDescription(dto.getDescription());
		flag.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
		flag.setStrategyType(dto.getStrategyType());
		flag.setStrategyConfig(dto.getStrategyConfig() != null ? dto.getStrategyConfig() : new HashMap<>());
		flag.setEnvironment(dto.getEnvironment() != null ? dto.getEnvironment() : "development");
		flag.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system");
		flag.setUpdatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system");

		FeatureFlag saved = flagRepository.save(flag);

		logAudit(saved, "CREATE", null, toAuditMap(saved), dto.getReason());

		return saved;
	}

	@CacheEvict(value = "featureFlags", key = "#flagKey")
	public FeatureFlag updateFlag(String flagKey, FeatureFlagDTO dto) {
		log.info("Updating flag: {}", flagKey);

		FeatureFlag existing = flagRepository.findByFlagKey(flagKey)
			.orElseThrow(() -> new FeatureFlagNotFoundException("Flag not found: " + flagKey));

		Map<String, Object> oldValues = toAuditMap(existing);

		if (dto.getDescription() != null) {
			existing.setDescription(dto.getDescription());
		}
		if (dto.getEnabled() != null) {
			existing.setEnabled(dto.getEnabled());
		}
		if (dto.getStrategyType() != null) {
			existing.setStrategyType(dto.getStrategyType());
		}
		if (dto.getStrategyConfig() != null) {
			existing.setStrategyConfig(dto.getStrategyConfig());
		}
		if (dto.getEnvironment() != null) {
			existing.setEnvironment(dto.getEnvironment());
		}

		existing.setUpdatedBy(dto.getUpdatedBy() != null ? dto.getUpdatedBy() : "system");

		FeatureFlag updated = flagRepository.save(existing);

		logAudit(updated, "UPDATE", oldValues, toAuditMap(updated), dto.getReason());

		return updated;
	}

	@CacheEvict(value = "featureFlags", key = "#flagKey")
	public void deleteFlag(String flagKey) {
		log.info("Deleting flag: {}", flagKey);

		FeatureFlag flag = flagRepository.findByFlagKey(flagKey)
			.orElseThrow(() -> new FeatureFlagNotFoundException("Flag not found: " + flagKey));

		Map<String, Object> oldValues = toAuditMap(flag);

		flagRepository.delete(flag);

		logAudit(flag, "DELETE", oldValues, null, "Flag deleted");
	}

	@CacheEvict(value = "featureFlags", key = "#flagKey")
	public FeatureFlag toggleFlag(String flagKey) {
		log.info("Toggling flag: {}", flagKey);

		FeatureFlag flag = flagRepository.findByFlagKey(flagKey)
			.orElseThrow(() -> new FeatureFlagNotFoundException("Flag not found: " + flagKey));

		Map<String, Object> oldValues = toAuditMap(flag);

		flag.setEnabled(!flag.getEnabled());
		flag.setUpdatedBy("system");

		FeatureFlag updated = flagRepository.save(flag);

		logAudit(updated, "TOGGLE", oldValues, toAuditMap(updated), "Flag toggled");

		return updated;
	}

	public Page<FlagAudit> getRecentAudits(int page, int size) {
		log.info("Fetching audit logs - page: {}, size: {}", page, size);
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
		return auditRepository.findAll(pageable);
	}

	public boolean evaluate(String flagKey, FlagContext context) {
		return evaluator.isEnabled(flagKey, context);
	}

	public List<FlagAudit> getAuditLog(String flagKey) {
		return auditRepository.findByFlagKeyOrderByTimestampDesc(flagKey);
	}

	public List<FlagAudit> getRecentAuditLog() {
		return auditRepository.findTop20ByOrderByTimestampDesc();
	}

	// Helper methods
	private void logAudit(FeatureFlag flag, String action, Map<String, Object> oldValue,
						  Map<String, Object> newValue, String reason) {
		FlagAudit audit = new FlagAudit();
		audit.setFlagId(flag.getId());
		audit.setFlagKey(flag.getFlagKey());
		audit.setAction(action);
		audit.setOldValue(oldValue);
		audit.setNewValue(newValue);
		audit.setChangedBy(flag.getUpdatedBy());
		audit.setReason(reason != null ? reason : "No reason provided");
		audit.setTimestamp(LocalDateTime.now());

		auditRepository.save(audit);
		log.info("Audit logged: {} on flag {}", action, flag.getFlagKey());
	}

	private Map<String, Object> toAuditMap(FeatureFlag flag) {
		Map<String, Object> map = new HashMap<>();
		map.put("enabled", flag.getEnabled());
		map.put("strategyType", flag.getStrategyType().toString());
		map.put("strategyConfig", flag.getStrategyConfig());
		map.put("environment", flag.getEnvironment());
		map.put("description", flag.getDescription());
		return map;
	}
}
