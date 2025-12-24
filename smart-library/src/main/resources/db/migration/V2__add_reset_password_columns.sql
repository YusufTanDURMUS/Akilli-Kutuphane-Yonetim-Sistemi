-- Şifre sıfırlama alanları ekle
ALTER TABLE users ADD COLUMN reset_token VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN reset_token_expiry TIMESTAMP NULL;

CREATE INDEX idx_reset_token ON users(reset_token);
