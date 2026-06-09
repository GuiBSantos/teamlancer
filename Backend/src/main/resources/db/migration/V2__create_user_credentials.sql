CREATE TABLE user_credentials (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    password_hash       VARCHAR(255) NOT NULL,
    refresh_token_hash  VARCHAR(255),
    last_login          TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_credentials_user_id ON user_credentials (user_id);

COMMENT ON TABLE  user_credentials                  IS 'Credenciais isoladas por segurança — nunca retornar via API';
COMMENT ON COLUMN user_credentials.password_hash    IS 'BCrypt hash da senha — nunca armazenar em texto plano';
COMMENT ON COLUMN user_credentials.refresh_token_hash IS 'Hash do refresh token — nulo quando sessão encerrada';
