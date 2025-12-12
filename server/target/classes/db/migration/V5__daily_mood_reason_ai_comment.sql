-- Add reason and AI comment to daily mood entries (renamed from V4 to V5 to resolve duplicate version conflict)
ALTER TABLE daily_mood_entries
    ADD COLUMN IF NOT EXISTS reason TEXT,
    ADD COLUMN IF NOT EXISTS ai_comment TEXT;