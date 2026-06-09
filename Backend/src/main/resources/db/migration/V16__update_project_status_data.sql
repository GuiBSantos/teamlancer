UPDATE projects SET status = 'IN_PROGRESS' WHERE status = 'ACTIVE';

COMMENT ON COLUMN projects.status IS
  'IN_PROGRESS = em andamento, IN_TESTING = em teste, COMPLETED = concluído, ON_HOLD = pausado, CANCELLED = cancelado';
