-- Flyway Migration V2: Align DB Schema with Entity Definitions
-- This migration updates the schema to match JPA entity constraints

-- =============================================
-- 1. USERS TABLE - unique_number NOT NULL + UNIQUE
-- =============================================

-- Generate unique_number for existing NULL records (12-char format)
UPDATE users
SET unique_number = 'U' || LPAD(FLOOR(RANDOM() * 99999999999)::TEXT, 11, '0')
WHERE unique_number IS NULL;

-- Add NOT NULL constraint
ALTER TABLE users ALTER COLUMN unique_number SET NOT NULL;

-- Change column length to varchar(12)
ALTER TABLE users ALTER COLUMN unique_number TYPE varchar(12);

-- Add UNIQUE constraint
ALTER TABLE users ADD CONSTRAINT uk_users_unique_number UNIQUE (unique_number);

-- =============================================
-- 2. PETS TABLE - unique_number NOT NULL + UNIQUE
-- =============================================

-- Generate unique_number for existing NULL records (12-char format)
UPDATE pets
SET unique_number = 'P' || LPAD(FLOOR(RANDOM() * 99999999999)::TEXT, 11, '0')
WHERE unique_number IS NULL;

-- Add NOT NULL constraint
ALTER TABLE pets ALTER COLUMN unique_number SET NOT NULL;

-- Change column length to varchar(12)
ALTER TABLE pets ALTER COLUMN unique_number TYPE varchar(12);

-- Add UNIQUE constraint
ALTER TABLE pets ADD CONSTRAINT uk_pets_unique_number UNIQUE (unique_number);

-- =============================================
-- 3. ORDERS TABLE - Add NOT NULL constraints
-- =============================================

-- Set default values for any NULL records (if any exist)
UPDATE orders SET recipient_name = '' WHERE recipient_name IS NULL;
UPDATE orders SET phone_number = '' WHERE phone_number IS NULL;
UPDATE orders SET zip_code = '' WHERE zip_code IS NULL;
UPDATE orders SET address = '' WHERE address IS NULL;

-- Add NOT NULL constraints
ALTER TABLE orders ALTER COLUMN recipient_name SET NOT NULL;
ALTER TABLE orders ALTER COLUMN phone_number SET NOT NULL;
ALTER TABLE orders ALTER COLUMN zip_code SET NOT NULL;
ALTER TABLE orders ALTER COLUMN address SET NOT NULL;

-- =============================================
-- 4. APPOINTMENTS TABLE - total_amount NOT NULL
-- =============================================

-- Set default value for any NULL records
UPDATE appointments SET total_amount = 0 WHERE total_amount IS NULL;

-- Add NOT NULL constraint
ALTER TABLE appointments ALTER COLUMN total_amount SET NOT NULL;

-- =============================================
-- 5. ADD MISSING INDEXES
-- =============================================

-- Orders indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);

-- Appointments indexes
CREATE INDEX IF NOT EXISTS idx_appointments_user_status ON appointments(user_id, status);
CREATE INDEX IF NOT EXISTS idx_appointments_time_slot ON appointments(time_slot_id, status);
