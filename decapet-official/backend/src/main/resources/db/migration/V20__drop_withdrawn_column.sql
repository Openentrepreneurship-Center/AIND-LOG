-- withdrawn 플래그 제거 (deletedAt으로 대체)
ALTER TABLE users DROP COLUMN IF EXISTS withdrawn;
