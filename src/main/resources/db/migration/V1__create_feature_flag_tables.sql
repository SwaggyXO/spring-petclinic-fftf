CREATE TABLE feature_flags (
                             id BIGSERIAL PRIMARY KEY,
                             flag_key VARCHAR(255) UNIQUE NOT NULL,
                             description TEXT,
                             enabled BOOLEAN DEFAULT false,
                             strategy_type VARCHAR(50) DEFAULT 'BOOLEAN',
                             strategy_config JSONB,
                             environment VARCHAR(50) DEFAULT 'development',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             created_by VARCHAR(100),
                             updated_by VARCHAR(100)
);

CREATE INDEX idx_flag_key ON feature_flags(flag_key);
CREATE INDEX idx_environment ON feature_flags(environment);

CREATE TABLE flag_audit_log (
                              id BIGSERIAL PRIMARY KEY,
                              flag_id BIGINT,
                              flag_key VARCHAR(255) NOT NULL,
                              action VARCHAR(50) NOT NULL,
                              old_value JSONB,
                              new_value JSONB,
                              changed_by VARCHAR(100),
                              reason TEXT,
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flag_audit_timestamp ON flag_audit_log(timestamp);
CREATE INDEX idx_flag_audit_key ON flag_audit_log(flag_key);
