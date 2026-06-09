CREATE TABLE team_portfolio (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id     UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    url         VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_team_portfolio_team_id ON team_portfolio (team_id);

COMMENT ON TABLE team_portfolio IS 'Projetos anteriores exibidos no perfil da equipe';
