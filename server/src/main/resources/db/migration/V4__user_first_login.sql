-- Add first_login column to anchor relative week calculations starting at first successful login
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS first_login timestamptz;