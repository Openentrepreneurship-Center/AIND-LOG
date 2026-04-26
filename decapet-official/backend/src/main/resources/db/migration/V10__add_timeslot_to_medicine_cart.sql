-- Flyway Migration V10: MedicineCart에 TimeSlot 연결 추가
-- 장바구니에 같은 TimeSlot의 의약품만 담을 수 있도록 제약

-- =============================================
-- 1. medicine_carts에 time_slot_id 컬럼 추가
-- =============================================
ALTER TABLE medicine_carts ADD COLUMN time_slot_id VARCHAR(26);

-- =============================================
-- 2. FK 제약조건 추가
-- =============================================
ALTER TABLE medicine_carts
ADD CONSTRAINT fk_medicine_carts_time_slot
FOREIGN KEY (time_slot_id) REFERENCES time_slots(id);

-- =============================================
-- 3. 인덱스 추가 (조회 성능 향상)
-- =============================================
CREATE INDEX IF NOT EXISTS idx_medicine_carts_time_slot ON medicine_carts(time_slot_id);
