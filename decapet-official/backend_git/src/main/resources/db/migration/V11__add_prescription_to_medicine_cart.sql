-- 의약품 장바구니에 처방전(Prescription) 지원 추가

-- item_type 컬럼 추가 (기본값 'MEDICINE')
ALTER TABLE medicine_cart_items
ADD COLUMN item_type VARCHAR(20) NOT NULL DEFAULT 'MEDICINE';

-- prescription_id 컬럼 추가
ALTER TABLE medicine_cart_items
ADD COLUMN prescription_id VARCHAR(26);

-- medicine_id nullable로 변경 (처방전인 경우 null)
ALTER TABLE medicine_cart_items
ALTER COLUMN medicine_id DROP NOT NULL;

-- medicine_name을 item_name으로 변경
ALTER TABLE medicine_cart_items
RENAME COLUMN medicine_name TO item_name;

-- 인덱스 추가 (처방전 조회 최적화)
CREATE INDEX idx_medicine_cart_items_prescription ON medicine_cart_items(prescription_id)
WHERE prescription_id IS NOT NULL;
