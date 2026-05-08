-- Payment ↔ Appointment 다중 관계를 위한 중간 테이블
CREATE TABLE payment_appointments (
    payment_id VARCHAR(26) NOT NULL REFERENCES payments(id),
    appointment_id VARCHAR(26) NOT NULL REFERENCES appointments(id),
    PRIMARY KEY (payment_id, appointment_id)
);

-- 기존 데이터 마이그레이션
INSERT INTO payment_appointments (payment_id, appointment_id)
SELECT id, appointment_id FROM payments WHERE appointment_id IS NOT NULL;

-- 기존 인덱스 제거
DROP INDEX IF EXISTS idx_payments_appointment;

-- 기존 컬럼 제거
ALTER TABLE payments DROP COLUMN IF EXISTS appointment_id;

-- 중간 테이블 인덱스
CREATE INDEX idx_payment_appointments_appointment ON payment_appointments(appointment_id);
