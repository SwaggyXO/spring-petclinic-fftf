package org.springframework.samples.petclinic.featureflag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.featureflag.model.FeatureFlag;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

	Optional<FeatureFlag> findByFlagKey(String flagKey);

	Optional<FeatureFlag> findByFlagKeyAndEnvironment(String flagKey, String environment);

	List<FeatureFlag> findByEnvironment(String environment);

	List<FeatureFlag> findByEnabled(Boolean enabled);

	boolean existsByFlagKey(String flagKey);
}
