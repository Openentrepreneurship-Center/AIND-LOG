-- item_type 컬럼 추가 (기본값 'MEDICINE')
ALTER TABLE appointment_medicine_items
ADD COLUMN item_type VARCHAR(20) NOT NULL DEFAULT 'MEDICINE';

-- prescription_id 컬럼 추가
ALTER TABLE appointment_medicine_items
ADD COLUMN prescription_id VARCHAR(26);

-- medicine_id NULL 허용 (처방전은 medicine_id가 없음)
ALTER TABLE appointment_medicine_items
ALTER COLUMN medicine_id DROP NOT NULL;

-- 처방전 인덱스 추가
CREATE INDEX idx_appointment_medicine_items_prescription
ON appointment_medicine_items(prescription_id)
WHERE prescription_id IS NOT NULL;
