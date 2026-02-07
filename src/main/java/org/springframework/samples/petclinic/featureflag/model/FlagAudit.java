package org.springframework.samples.petclinic.featureflag.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "flag_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long flagId;

	@Column(nullable = false)
	private String flagKey;

	@Column(nullable = false)
	private String action;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> oldValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> newValue;

	private String changedBy;

	private String reason;

	private LocalDateTime timestamp = LocalDateTime.now();
}
