CREATE TYPE rater_type AS ENUM ('CLIENT', 'TEAM');

CREATE TABLE ratings (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id  UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    rater_id    UUID        NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    rater_type  rater_type  NOT NULL,
    score       INTEGER     NOT NULL CHECK (score >= 1 AND score <= 5),
    comment     TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_rating_per_project UNIQUE (project_id, rater_type)
);

CREATE INDEX idx_ratings_project_id ON ratings (project_id);
CREATE INDEX idx_ratings_rater_id   ON ratings (rater_id);

COMMENT ON TABLE  ratings            IS 'Avaliações mútuas após conclusão do projeto';
COMMENT ON COLUMN ratings.rater_type IS 'CLIENT = cliente avaliando o time, TEAM = time avaliando o cliente';
COMMENT ON COLUMN ratings.score      IS 'De 1 a 5 estrelas';
