-- 처방전을 TimeSlot 기반에서 User 기반으로 전환

-- 1. user_id 컬럼 추가 (nullable로 먼저)
ALTER TABLE prescriptions ADD COLUMN user_id VARCHAR(255);

-- 2. 기존 데이터 마이그레이션: pet의 user_id로 채움
UPDATE prescriptions p
SET user_id = pet.user_id
FROM pets pet
WHERE p.pet_id = pet.id;

-- 3. NOT NULL 제약 추가
ALTER TABLE prescriptions ALTER COLUMN user_id SET NOT NULL;

-- 4. FK 추가
ALTER TABLE prescriptions ADD CONSTRAINT fk_prescriptions_user
    FOREIGN KEY (user_id) REFERENCES users(id);

-- 5. time_slot_id FK 및 컬럼 제거
ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS fk_prescriptions_time_slot;
ALTER TABLE prescriptions DROP COLUMN IF EXISTS time_slot_id;

-- 6. 인덱스 교체
DROP INDEX IF EXISTS idx_prescriptions_time_slot;
CREATE INDEX idx_prescriptions_user ON prescriptions(user_id);
