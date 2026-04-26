-- 기존 unique index 삭제 (user_id + time_slot_id)
DROP INDEX IF EXISTS idx_appointment_user_timeslot;

-- 새 unique index 생성 (user_id + pet_id + time_slot_id)
CREATE UNIQUE INDEX idx_appointment_user_pet_timeslot
ON appointments (user_id, pet_id, time_slot_id)
WHERE deleted_at IS NULL AND status IN ('PENDING', 'APPROVED', 'COMPLETED');
