CREATE TYPE request_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'REJECTED',
    'CANCELLED'
);

CREATE TABLE project_requests (
    id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id    UUID           NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    team_id      UUID           NOT NULL REFERENCES teams(id) ON DELETE RESTRICT,
    project_name VARCHAR(200)   NOT NULL,
    description  TEXT           NOT NULL,
    budget_range VARCHAR(100),
    deadline     VARCHAR(100),
    status       request_status NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_project_requests_client_id ON project_requests (client_id);
CREATE INDEX idx_project_requests_team_id   ON project_requests (team_id);
CREATE INDEX idx_project_requests_status    ON project_requests (status);
CREATE INDEX idx_project_requests_created   ON project_requests (created_at DESC);

COMMENT ON TABLE  project_requests        IS 'Solicitações de projeto enviadas por clientes às equipes';
COMMENT ON COLUMN project_requests.status IS 'PENDING → ACCEPTED (vira projeto) ou REJECTED. Cliente pode CANCELLED';
