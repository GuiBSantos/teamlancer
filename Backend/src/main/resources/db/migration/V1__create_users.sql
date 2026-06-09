CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('CLIENT', 'MEMBER', 'ADMIN');

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    avatar_url  VARCHAR(500),
    role        user_role    NOT NULL DEFAULT 'CLIENT',
    location    VARCHAR(150),
    bio         TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email  ON users (email);
CREATE INDEX idx_users_role   ON users (role);
CREATE INDEX idx_users_active ON users (active);

COMMENT ON TABLE  users            IS 'Usuários unificados — clientes e membros de equipe';
COMMENT ON COLUMN users.role       IS 'CLIENT = contratante, MEMBER = membro de equipe, ADMIN = admin da plataforma';
COMMENT ON COLUMN users.active     IS 'Soft delete — false oculta o usuário sem remover dados';
