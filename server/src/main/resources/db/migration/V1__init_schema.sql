-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

-- Enable pgcrypto for gen_random_uuid (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Users first (referenced by moods)
CREATE TABLE IF NOT EXISTS public.users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username text NOT NULL UNIQUE,
    last_login timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    password_hash text,
    timestamp timestamptz DEFAULT now()
);

-- Moods table
CREATE TABLE IF NOT EXISTS public.moods (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    mood text NOT NULL,
    score integer NOT NULL CHECK (score >= 0 AND score <= 10),
    timestamp timestamptz NOT NULL DEFAULT now(),
    user_id uuid REFERENCES public.users(id) ON DELETE SET NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);