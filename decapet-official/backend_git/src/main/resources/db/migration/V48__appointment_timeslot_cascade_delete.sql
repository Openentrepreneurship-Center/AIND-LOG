-- Appointment의 time_slot_id FK를 CASCADE DELETE로 변경
-- 타임슬롯 삭제 시 연결된 예약도 함께 삭제됨

-- 기존 FK 제약 찾아서 삭제 후 재생성
DO $$
DECLARE
    fk_name TEXT;
BEGIN
    SELECT tc.constraint_name INTO fk_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'appointments'
      AND kcu.column_name = 'time_slot_id'
      AND tc.constraint_type = 'FOREIGN KEY';

    IF fk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE appointments DROP CONSTRAINT %I', fk_name);
    END IF;
END $$;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_time_slot
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE CASCADE;
