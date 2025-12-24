-- Add indexes to speed up token lookups and reduce collisions
ALTER TABLE users
    ADD UNIQUE INDEX idx_users_verification_token (verification_token),
    ADD UNIQUE INDEX idx_users_reset_token (reset_token);
