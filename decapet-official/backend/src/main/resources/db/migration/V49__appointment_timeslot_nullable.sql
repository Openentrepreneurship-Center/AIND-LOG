-- V48에서 추가한 CASCADE DELETE를 제거하고, nullable로 변경
-- soft-delete 방식: 코드에서 time_slot_id를 null로 설정 후 soft-delete

-- CASCADE FK 제거 후 일반 FK로 재생성 (nullable)
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointments_time_slot;

ALTER TABLE appointments ALTER COLUMN time_slot_id DROP NOT NULL;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_time_slot
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id);
