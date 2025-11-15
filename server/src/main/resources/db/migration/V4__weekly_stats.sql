CREATE TABLE IF NOT EXISTS weekly_stats (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    week_number INT NOT NULL,
    average_score DOUBLE PRECISION,
    entries_count INT,
    breakdown_json TEXT,
    ai_comment TEXT,
    activities_json TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_weekly_stats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_weekly_stats_user_week UNIQUE (user_id, week_number)
);
