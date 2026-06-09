CREATE TABLE team_members (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id      UUID         NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_in_team VARCHAR(100) NOT NULL,
    joined_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_team_member UNIQUE (team_id, user_id)
);

CREATE INDEX idx_team_members_team_id ON team_members (team_id);
CREATE INDEX idx_team_members_user_id ON team_members (user_id);

COMMENT ON TABLE  team_members              IS 'Relação N:N entre equipes e usuários';
COMMENT ON COLUMN team_members.role_in_team IS 'Ex: Frontend Dev, Backend Dev, UI/UX Designer, Product Manager';
