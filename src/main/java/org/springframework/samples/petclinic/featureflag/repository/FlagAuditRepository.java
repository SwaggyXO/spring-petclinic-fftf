package org.springframework.samples.petclinic.featureflag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.featureflag.model.FlagAudit;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlagAuditRepository extends JpaRepository<FlagAudit, Long> {

	List<FlagAudit> findByFlagKeyOrderByTimestampDesc(String flagKey);

	List<FlagAudit> findByFlagIdOrderByTimestampDesc(Long flagId);

	List<FlagAudit> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

	List<FlagAudit> findTop20ByOrderByTimestampDesc();
}
