-- Create table for daily mood placeholders (one row per user per date)
CREATE TABLE IF NOT EXISTS public.daily_mood_entries (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid REFERENCES public.users(id) ON DELETE CASCADE,
    entry_date date NOT NULL,
    mood integer,
    created_at timestamptz DEFAULT now(),
    week_number integer DEFAULT 0,
    day_name text,
    CONSTRAINT uq_daily_mood_user_date UNIQUE (user_id, entry_date)
);
