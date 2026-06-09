CREATE TABLE teams (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id    UUID         NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    tech_stack  TEXT[]       NOT NULL DEFAULT '{}',
    location    VARCHAR(150),
    team_score  INTEGER      NOT NULL DEFAULT 0 CHECK (team_score >= 0 AND team_score <= 100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_teams_slug       ON teams (slug);
CREATE INDEX idx_teams_owner_id   ON teams (owner_id);
CREATE INDEX idx_teams_active     ON teams (active);
CREATE INDEX idx_teams_score      ON teams (team_score DESC);
CREATE INDEX idx_teams_tech_stack ON teams USING GIN (tech_stack);

COMMENT ON TABLE  teams             IS 'Equipes disponíveis para contratação';
COMMENT ON COLUMN teams.slug        IS 'Identificador amigável para URL — ex: team-nova';
COMMENT ON COLUMN teams.tech_stack  IS 'Array de tecnologias — ex: {React, Node, Postgres}';
COMMENT ON COLUMN teams.team_score  IS 'Score calculado de 0-100 baseado em projetos e avaliações';
