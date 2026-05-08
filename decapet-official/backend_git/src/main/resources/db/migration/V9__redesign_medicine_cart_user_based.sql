-- Flyway Migration V9: Redesign MedicineCart to User-based
-- 반려동물 기반 → 사용자 기반으로 변경
-- medicine_carts: user_id + pet_id (unique) → user_id (unique)
-- medicine_cart_items: pet_id, pet_name 필드 추가

-- =============================================
-- 1. medicine_cart_items에 pet_id, pet_name, image_url 컬럼 추가 (nullable)
-- =============================================
ALTER TABLE medicine_cart_items ADD COLUMN pet_id VARCHAR(26);
ALTER TABLE medicine_cart_items ADD COLUMN pet_name VARCHAR(100);
ALTER TABLE medicine_cart_items ADD COLUMN image_url VARCHAR(500);

-- =============================================
-- 2. 기존 데이터 마이그레이션
-- medicine_carts의 pet_id를 medicine_cart_items로 복사
-- =============================================
UPDATE medicine_cart_items mci
SET pet_id = mc.pet_id,
    pet_name = (SELECT name FROM pets p WHERE p.id = mc.pet_id)
FROM medicine_carts mc
WHERE mci.cart_id = mc.id;

-- =============================================
-- 3. NOT NULL 제약조건 추가
-- =============================================
ALTER TABLE medicine_cart_items ALTER COLUMN pet_id SET NOT NULL;
ALTER TABLE medicine_cart_items ALTER COLUMN pet_name SET NOT NULL;

-- =============================================
-- 4. 동일 사용자의 여러 장바구니를 하나로 병합
-- =============================================

-- 4-1. 각 사용자의 첫 번째(가장 오래된) 장바구니 ID 찾기
CREATE TEMP TABLE user_primary_cart AS
SELECT DISTINCT ON (user_id) id as primary_cart_id, user_id
FROM medicine_carts
ORDER BY user_id, created_at ASC;

-- 4-2. 다른 장바구니의 아이템들을 첫 번째 장바구니로 이동
UPDATE medicine_cart_items mci
SET cart_id = upc.primary_cart_id
FROM medicine_carts mc
JOIN user_primary_cart upc ON mc.user_id = upc.user_id
WHERE mci.cart_id = mc.id
  AND mc.id != upc.primary_cart_id;

-- 4-3. 병합된(비어있는) 장바구니 삭제
DELETE FROM medicine_carts mc
WHERE NOT EXISTS (
    SELECT 1 FROM medicine_cart_items mci WHERE mci.cart_id = mc.id
)
AND EXISTS (
    SELECT 1 FROM user_primary_cart upc
    WHERE upc.user_id = mc.user_id AND upc.primary_cart_id != mc.id
);

-- 4-4. 임시 테이블 삭제
DROP TABLE user_primary_cart;

-- =============================================
-- 5. medicine_carts에서 pet_id 관련 제약조건 및 컬럼 제거
-- =============================================

-- FK 제약조건 제거
ALTER TABLE medicine_carts DROP CONSTRAINT IF EXISTS fk_medicine_carts_pet;

-- 기존 unique constraint 제거 (있는 경우)
ALTER TABLE medicine_carts DROP CONSTRAINT IF EXISTS idx_medicine_carts_user_pet;
ALTER TABLE medicine_carts DROP CONSTRAINT IF EXISTS uk_medicine_carts_user_pet;

-- pet_id 컬럼 제거
ALTER TABLE medicine_carts DROP COLUMN pet_id;

-- =============================================
-- 6. 새로운 unique constraint 추가 (user_id only)
-- =============================================
ALTER TABLE medicine_carts ADD CONSTRAINT idx_medicine_carts_user UNIQUE (user_id);

-- =============================================
-- 7. medicine_cart_items에 인덱스 추가
-- =============================================
CREATE INDEX IF NOT EXISTS idx_medicine_cart_items_pet ON medicine_cart_items(pet_id);
