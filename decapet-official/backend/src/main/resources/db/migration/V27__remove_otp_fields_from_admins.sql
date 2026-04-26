-- Remove OTP fields from admins table (OTP authentication removed)
ALTER TABLE admins DROP COLUMN IF EXISTS otp_secret;
ALTER TABLE admins DROP COLUMN IF EXISTS otp_enabled;
