CREATE TABLE chat_messages (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id  UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sender_id   UUID        NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    content     TEXT        NOT NULL,
    read_at     TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_project_id ON chat_messages (project_id);
CREATE INDEX idx_chat_messages_sender_id  ON chat_messages (sender_id);
CREATE INDEX idx_chat_messages_created    ON chat_messages (created_at ASC);

COMMENT ON TABLE  chat_messages            IS 'Mensagens de chat por projeto — entre cliente e equipe';
COMMENT ON COLUMN chat_messages.read_at    IS 'NULL = não lida pelo destinatário';
