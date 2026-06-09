CREATE TYPE project_status AS ENUM (
    'ACTIVE',      -- em andamento
    'COMPLETED',   -- concluído
    'ON_HOLD',     -- pausado
    'CANCELLED'    -- cancelado após aceite
);

CREATE TABLE projects (
    id          UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id  UUID           NOT NULL UNIQUE REFERENCES project_requests(id) ON DELETE RESTRICT,
    client_id   UUID           NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    team_id     UUID           NOT NULL REFERENCES teams(id) ON DELETE RESTRICT,
    name        VARCHAR(200)   NOT NULL,
    description TEXT,
    status      project_status NOT NULL DEFAULT 'ACTIVE',
    started_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMP,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_projects_client_id  ON projects (client_id);
CREATE INDEX idx_projects_team_id    ON projects (team_id);
CREATE INDEX idx_projects_status     ON projects (status);
CREATE INDEX idx_projects_request_id ON projects (request_id);

COMMENT ON TABLE  projects            IS 'Projetos ativos — criados automaticamente quando request é ACCEPTED';
COMMENT ON COLUMN projects.request_id IS 'Referência à solicitação de origem — 1:1 obrigatório';
