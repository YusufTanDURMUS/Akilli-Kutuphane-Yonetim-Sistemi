-- Kullanıcı e-posta doğrulama alanları
ALTER TABLE users ADD COLUMN verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN verification_token VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN verification_token_expiry TIMESTAMP NULL;

CREATE INDEX idx_verification_token ON users(verification_token);
