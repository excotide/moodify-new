ALTER TABLE weekly_stats ADD COLUMN IF NOT EXISTS fingerprint VARCHAR(512);
CREATE INDEX IF NOT EXISTS idx_weekly_stats_user_week ON weekly_stats(user_id, week_number);
