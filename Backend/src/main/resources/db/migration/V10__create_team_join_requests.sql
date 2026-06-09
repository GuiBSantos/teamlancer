CREATE TYPE join_request_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED');

CREATE TABLE team_join_requests (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id     UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message     TEXT,
    status      join_request_status NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_team_join_request UNIQUE (team_id, user_id, status)
);

CREATE INDEX idx_team_join_requests_team ON team_join_requests(team_id);
CREATE INDEX idx_team_join_requests_user ON team_join_requests(user_id);
