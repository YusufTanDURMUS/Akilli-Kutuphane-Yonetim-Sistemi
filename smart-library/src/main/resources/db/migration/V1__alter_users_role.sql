-- Ensure role column can hold enum names
ALTER TABLE users MODIFY role VARCHAR(20);