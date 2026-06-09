CREATE TABLE audit_logs (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID         REFERENCES users(id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   UUID,
    metadata    JSONB,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id     ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created     ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_action      ON audit_logs (action);
CREATE INDEX idx_audit_logs_metadata    ON audit_logs USING GIN (metadata);

COMMENT ON TABLE  audit_logs             IS 'Log imutável de ações relevantes no sistema';
COMMENT ON COLUMN audit_logs.action      IS 'Ex: USER_REGISTER, TEAM_CREATED, REQUEST_SENT, REQUEST_ACCEPTED';
COMMENT ON COLUMN audit_logs.entity_type IS 'Ex: USER, TEAM, PROJECT_REQUEST, PROJECT';
COMMENT ON COLUMN audit_logs.metadata    IS 'Dados extras em JSON — ex: {"old_status":"PENDING","new_status":"ACCEPTED"}';
COMMENT ON COLUMN audit_logs.user_id     IS 'NULL se ação de sistema ou usuário deletado';
