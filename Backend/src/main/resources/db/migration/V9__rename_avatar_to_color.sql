ALTER TABLE users RENAME COLUMN avatar_url TO avatar_color;
ALTER TABLE users ALTER COLUMN avatar_color TYPE VARCHAR(20);
