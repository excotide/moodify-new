-- Add password_hash column if not exists and ensure username uniqueness (idempotent for PostgreSQL)
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password_hash text;
-- Unique index (if table already had UNIQUE constraint this will be skipped if name matches)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'users_username_idx') THEN
        CREATE UNIQUE INDEX users_username_idx ON public.users (username);
    END IF;
END$$;
